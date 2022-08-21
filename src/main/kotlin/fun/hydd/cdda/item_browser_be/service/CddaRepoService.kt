package `fun`.hydd.cdda.item_browser_be.service

import `fun`.hydd.cdda.item_browser_be.entity.GitTag
import io.vertx.kotlin.coroutines.awaitBlocking
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * Cdda git repo service
 * @constructor
 */
class CddaRepoService(private val git: Git) {
  private val log = LoggerFactory.getLogger(CddaRepoService::class.java)

  suspend fun update() {
    awaitBlocking {
      git.pull().setRemote(Constants.DEFAULT_REMOTE_NAME).setRemoteBranchName(Constants.MASTER).call()
    }
  }

  suspend fun rest(tagName: String) {
    awaitBlocking {
      git.reset().setMode(ResetCommand.ResetType.HARD)
        .setRef(tagName)
        .call()
    }
  }

  /**
   * get need updated gitTag list
   * @param dbTagDate OffsetDateTime? db latest version tag date
   * @return List<GitTag> result
   */
  fun getAllNeedUpdateGitTag(dbTagDate: OffsetDateTime?): List<GitTag> {
    val latestGitTag = getLatestGitTag()
    return if (latestGitTag == null) {
      listOf()
    } else if (dbTagDate == null) {
      listOf(latestGitTag)
    } else if (latestGitTag.date == dbTagDate || latestGitTag.date.isBefore(dbTagDate)) {
      listOf()
    } else {
      getAfterTagList(dbTagDate)
    }
  }

  private fun getLatestGitTag(): GitTag? {
    val tagRef = getLatestTagRef()
    return if (tagRef != null)
      tagRef2GitTag(tagRef)
    else null
  }

  private fun getAfterTagList(date: OffsetDateTime): List<GitTag> {
    val localRefs = git.tagList()
      .call()
    val result = ArrayList<GitTag>()
    for (localRef in localRefs) {
      val gitTag = tagRef2GitTag(localRef)
      if (gitTag != null && gitTag.date.isAfter(date)) {
        result.add(gitTag)
      }
    }
    return result.sortedBy { it.date }
  }

  private fun tagRef2GitTag(tagRef: Ref): GitTag? {
    RevWalk(git.repository).use { revWalk ->
      val revObject = revWalk.parseAny(tagRef.objectId)
      if (Constants.OBJ_TAG == revObject.type) {
        val revTag = revObject as RevTag
        return GitTag(
          revTag.tagName, revTag.taggerIdent.getWhen().toInstant()
            .atOffset(ZoneOffset.UTC)
        )
      } else if (Constants.OBJ_COMMIT == revObject.type) {
        val revCommit = revObject as RevCommit
        return GitTag(
          revCommit.name, revCommit.authorIdent.getWhen().toInstant()
            .atOffset(ZoneOffset.UTC)
        )
      } else {
        return null
      }
    }
  }

  private fun getLatestTagRef(): Ref? {
    var result: Ref? = null
    var latestDate: Date? = null
    val tagRefs = git.tagList().call()
    for (tagRef in tagRefs) {
      val currentDate: Date = getTagRefDate(tagRef)
      if (latestDate == null || currentDate.after(latestDate)) {
        result = tagRef
        latestDate = currentDate
      }
    }
    return result
  }

  private fun getTagRefDate(tagRef: Ref): Date {
    RevWalk(git.repository).use { revWalk ->
      val revObject = revWalk.parseAny(tagRef.objectId)
      if (Constants.OBJ_TAG == revObject.type) {
        val revTag = revObject as RevTag
        return revTag.taggerIdent.getWhen()
      } else if (Constants.OBJ_COMMIT == revObject.type) {
        val revCommit = revObject as RevCommit
        return revCommit.authorIdent.getWhen()
      } else {
        log.error(
          "getTagRefDate ragRef no tag or commit, type is {}, id is {}", revObject.type, tagRef.objectId
        )
        throw NullPointerException("Wrong tagRef type")
      }
    }
  }
}
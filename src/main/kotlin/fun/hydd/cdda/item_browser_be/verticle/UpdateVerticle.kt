package `fun`.hydd.cdda.item_browser_be.verticle

import `fun`.hydd.cdda.item_browser_be.constant.VersionStatus
import `fun`.hydd.cdda.item_browser_be.entity.Version
import `fun`.hydd.cdda.item_browser_be.service.CddaRepoService
import `fun`.hydd.cdda.item_browser_be.service.GithubService
import `fun`.hydd.cdda.item_browser_be.service.VersionService
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.time.ZoneOffset
import javax.persistence.Persistence


class UpdateVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(UpdateVerticle::class.java)
  private lateinit var cddaRepoService: CddaRepoService
  private lateinit var versionService: VersionService
  private lateinit var githubService: GithubService

  override suspend fun start() {
    initVar()
    update()
  }

  private suspend fun update() {
    val dbVersion = versionService.getLatestVersion()
    if (dbVersion?.status == VersionStatus.FAIL) {
      log.error("db have fail version, ${dbVersion.tagName}")
      return
    }
    val versions = getNeedUpdateVersions(dbVersion)
    versionService.saveVersions(versions)
  }

  private suspend fun getNeedUpdateVersions(dbVersion: Version?) = coroutineScope {
    cddaRepoService.getAllNeedUpdateGitTag(dbVersion?.tagDate).map {
      async {
        val githubRelease = githubService.getReleaseByTagName(it.name)
        if (githubRelease != null) {
          Version(
            VersionStatus.PEND,
            it.name,
            githubRelease.name,
            githubRelease.isExperiment,
            githubRelease.commitHash,
            githubRelease.publishDate.toInstant().atOffset(ZoneOffset.UTC),
            it.date,
            listOf(),
            listOf(),
            null
          )
        } else {
          null
        }
      }
    }.awaitAll().filterNotNull()
  }

  private suspend fun initVar() {
    githubService = GithubService(vertx)
    coroutineScope {
      listOf(
        async { cddaRepoService = CddaRepoService(initGit()) },
        async { versionService = VersionService(initEmf()) }
      ).awaitAll()
    }
  }

  private suspend fun initEmf(): Stage.SessionFactory {
    return awaitBlocking {
      Persistence.createEntityManagerFactory("item-browser").unwrap(Stage.SessionFactory::class.java)
    }
  }

  private suspend fun initGit(): Git {
    val repositoryPath = Paths.get(System.getProperty("user.home"), "Cataclysm-DDA").toFile()
    return vertx.executeBlocking {
      if (repositoryPath.exists()) {
        it.complete(Git.open(repositoryPath))
      } else {
        it.complete(
          Git.cloneRepository().setDirectory(repositoryPath)
            .setURI("https://github.com/CleverRaven/Cataclysm-DDA.git")
            .setBranch(Constants.MASTER).call()
        )
      }
    }.await()
  }
}
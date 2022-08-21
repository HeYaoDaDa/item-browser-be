package `fun`.hydd.cdda.item_browser_be.service

import `fun`.hydd.cdda.item_browser_be.entity.Version
import `fun`.hydd.cdda.item_browser_be.util.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage

class VersionService(private val emf: Stage.SessionFactory) {

  suspend fun getAll(): List<Version> {
    return emf.withSession {
      it.createQuery<Version>("FROM version").resultList
    }.await()
  }

  suspend fun getLatestVersion(): Version? {
    return emf.withSession {
      it.createQuery<Version>("FROM Version ORDER BY publishDate DESC").singleResultOrNull
    }.await()
  }

  suspend fun saveVersion(version: Version) {
    emf.withTransaction { session, _ -> session.persist(version) }.await()
  }

  suspend fun saveVersions(versions: List<Version>) {
    coroutineScope {
      versions.map { async { saveVersion(it) } }.awaitAll()
    }
  }
}


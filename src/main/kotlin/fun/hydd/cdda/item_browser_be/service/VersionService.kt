package `fun`.hydd.cdda.item_browser_be.service

import `fun`.hydd.cdda.item_browser_be.entity.Version
import `fun`.hydd.cdda.item_browser_be.util.await
import `fun`.hydd.cdda.item_browser_be.util.toFuture
import io.vertx.core.CompositeFuture
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
    emf.withTransaction { session, _ ->
      CompositeFuture.all(versions.map { session.persist(it).toFuture() }).toCompletionStage()
    }.await()
  }
}


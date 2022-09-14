package `fun`.hydd.cdda.item_browser_be.verticle

import `fun`.hydd.cdda.item_browser_be.constant.VersionStatus
import `fun`.hydd.cdda.item_browser_be.entity.CddaItem
import `fun`.hydd.cdda.item_browser_be.entity.HashJson
import `fun`.hydd.cdda.item_browser_be.entity.Version
import `fun`.hydd.cdda.item_browser_be.model.CddaMod
import `fun`.hydd.cdda.item_browser_be.model.cddaModOf
import `fun`.hydd.cdda.item_browser_be.model.setAllModDepMods
import `fun`.hydd.cdda.item_browser_be.model.sortMods
import `fun`.hydd.cdda.item_browser_be.service.CddaRepoService
import `fun`.hydd.cdda.item_browser_be.service.GithubService
import `fun`.hydd.cdda.item_browser_be.service.VersionService
import `fun`.hydd.cdda.item_browser_be.util.getHashString
import io.vertx.core.json.JsonObject
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
import java.io.File
import java.io.FileNotFoundException
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
    log.info("need updated version size is ${versions.size}, content is ${versions.joinToString { it.tagName + "| " }}")
    versions.map {
      cddaRepoService.rest(it.tagName)
      val repositoryPath = Paths.get(System.getProperty("user.home"), "Cataclysm-DDA").toFile()
      val modsPath = Paths.get(repositoryPath.absolutePath, "data", "mods").toFile()
      val modPaths = modsPath.listFiles { file -> file.isDirectory }
      if (modPaths != null) {
        val soredMods = sortMods(getCddaModsByPaths(modPaths.toList()))
        setAllModDepMods(soredMods)
      }
    }
  }

  /**
   * [getCddaModByPath] list async method
   * @param modPaths List<File>
   * @return List<CddaMod>
   */
  private suspend fun getCddaModsByPaths(modPaths: List<File>): List<CddaMod> = coroutineScope {
    modPaths.map { async { getCddaModByPath(it) } }.awaitAll()
  }

  /**
   * from mod's directory file construct CddaMod
   * @param modPath File mod directory file
   * @return CddaMod
   */
  private suspend fun getCddaModByPath(modPath: File): CddaMod {
    val modinfoFile = modPath.listFiles { file -> file.isFile && file.name == "modinfo.json" }?.firstOrNull()
    if (modinfoFile == null) throw FileNotFoundException("mondinfo.json file no found in ${modPath.absolutePath}")
    else {
      val modJsonObject = vertx.fileSystem().readFile(modinfoFile.absolutePath).await().toJsonArray()
        .mapNotNull { if (it is JsonObject) it else null }.first {
          it.containsKey("type") && it.getString("type").lowercase() == "mod_info"
        }
      return cddaModOf(modJsonObject)
    }
  }

  private fun topologySort(modTriples: List<Triple<String, List<String>, List<CddaItem>>>): List<Triple<String, List<String>, List<CddaItem>>> {
    val resultModId = mutableListOf<String>()
    val myMods = modTriples.map { Triple(it.first, it.second.toMutableList(), it.third) }.toMutableList()
    while (myMods.isNotEmpty()) {
      val beforeSize = myMods.size
      myMods.filter { it.second.isEmpty() }.map { resultModId.add(it.first);myMods.remove(it) }
      myMods.map { it.second.removeIf { depModId -> resultModId.contains(depModId) } }
      if (myMods.size == beforeSize) {
        log.error("have mod no dependent mod: $myMods")
        break
      }
    }
    return resultModId.map { modId -> modTriples.first { it.first == modId } }
  }

  private suspend fun getModInfoJsonObject(modPath: File): JsonObject {
    val modinfoFile = modPath.listFiles { file -> file.isFile && file.name == "modinfo.json" }?.firstOrNull()
    if (modinfoFile == null) throw FileNotFoundException("mondinfo.json file no found in ${modPath.absolutePath}")
    else return vertx.fileSystem().readFile(modinfoFile.absolutePath).await().toJsonArray()
      .mapNotNull { if (it is JsonObject) it else null }.first {
        it.containsKey("type") && it.getString("type").lowercase() == "mod_info"
      }
  }

  private suspend fun getJsonObjectsByFile(file: File): List<JsonObject> = coroutineScope {
    vertx.fileSystem().readFile(file.absolutePath).await().toJsonArray()
      .mapNotNull { if (it is JsonObject) it else null }
  }

  private suspend fun getJsonObjectsByModDir(dataDir: File, modDir: File, modId: String): List<CddaItem> =
    coroutineScope {
      (if (modId == "dda") getDDAJsonFileInDataDir(modDir) else getAllJsonFileInDir(modDir)).map {
        async {
          getJsonObjectsByFile(it).filter { it.containsKey("type") }.map {
            CddaItem(
              null,
              null,
              null,
              null,
              mutableListOf(),
              it.getString("type"),
              modId,
              Paths.get(dataDir.toURI()).relativize(Paths.get(modDir.toURI())).toString(),
              HashJson(it.getHashString(), it),
              null,
              null,
              null
            )
          }
        }
      }
    }.awaitAll().flatten()


  private fun getAllJsonFileInDir(dirPath: File): List<File> {
    val result: MutableList<File> = ArrayList()
    val dirFiles = dirPath.listFiles()
    if (dirFiles != null && dirFiles.isNotEmpty()) {
      for (file in dirFiles) {
        if (file.isDirectory) {
          result.addAll(getAllJsonFileInDir(file))
        } else if (file.absolutePath.endsWith(".json")) {
          result.add(file)
        }
      }
    }
    return result
  }

  private fun getDDAJsonFileInDataDir(dataPath: File): List<File> {
    val ddaDirPath = listOf(
      Paths.get(dataPath.absolutePath, "core").toString(),
      Paths.get(dataPath.absolutePath, "help").toString(),
      Paths.get(dataPath.absolutePath, "json").toString(),
      Paths.get(dataPath.absolutePath, "raw").toString(),
      Paths.get(dataPath.absolutePath, "mods", "dda").toString()
    )
    val result: MutableList<File> = java.util.ArrayList()
    for (path in ddaDirPath) {
      result.addAll(getAllJsonFileInDir(File(path)))
    }
    return result
  }

  private suspend fun getNeedUpdateVersions(dbVersion: Version?) = coroutineScope {
    cddaRepoService.getAllNeedUpdateGitTag(dbVersion?.tagDate).map {
      async {
        val githubRelease = githubService.getReleaseByTagName(it.name)
        if (githubRelease != null) {
          Version(
            VersionStatus.PROCESS,
            it.name,
            githubRelease.name,
            githubRelease.isExperiment,
            githubRelease.commitHash,
            githubRelease.publishDate.toInstant().atOffset(ZoneOffset.UTC),
            it.date,
            mutableListOf(),
            mutableListOf(),
            null
          )
        } else {
          null
        }
      }
    }.awaitAll().filterNotNull()
  }

  private suspend fun initVar() = coroutineScope {
    listOf(async { githubService = GithubService(vertx) },
      async { cddaRepoService = CddaRepoService(initGit()) },
      async { versionService = VersionService(initEmf()) }).awaitAll()
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
          Git.cloneRepository().setDirectory(repositoryPath).setURI("https://github.com/CleverRaven/Cataclysm-DDA.git")
            .setBranch(Constants.MASTER).call()
        )
      }
    }.await()
  }
}
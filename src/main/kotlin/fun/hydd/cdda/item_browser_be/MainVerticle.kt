package `fun`.hydd.cdda.item_browser_be

import `fun`.hydd.cdda.item_browser_be.verticle.UpdateVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import org.slf4j.LoggerFactory


class MainVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(MainVerticle::class.java)

  override suspend fun start() {
    deployVerticle(readConfig())
    log.info("MainVerticle start success")
  }

  private suspend fun readConfig(): JsonObject {
    val options = ConfigRetrieverOptions()
      .addStore(configStoreOptionsOf(type = "file", config = jsonObjectOf("path" to "conf/config.json")))
      .addStore(configStoreOptionsOf(type = "env"))
    return ConfigRetriever.create(vertx, options).config.await()
  }

  private suspend fun deployVerticle(config: JsonObject): String? {
    val options = deploymentOptionsOf(config = config)
    vertx.exceptionHandler { obj: Throwable -> obj.printStackTrace() }
    return vertx.deployVerticle(UpdateVerticle::class.java, options).await()
  }
}

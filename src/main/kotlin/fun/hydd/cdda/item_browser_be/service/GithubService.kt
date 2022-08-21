package `fun`.hydd.cdda.item_browser_be.service

import `fun`.hydd.cdda.item_browser_be.entity.GithubRelease
import `fun`.hydd.cdda.item_browser_be.util.HttpUtil
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.JsonObject

class GithubService(val vertx: Vertx) {
  suspend fun getReleaseByTagName(tagName: String): GithubRelease? {
    val requestOptions: RequestOptions =
      RequestOptions().setHost("api.github.com").setURI("/repos/CleverRaven/Cataclysm-DDA/releases/tags/$tagName")
        .setMethod(HttpMethod.GET).setPort(443).putHeader("User-Agent", "item-browser").setSsl(true)
    val buffer = HttpUtil.request(vertx, requestOptions)
    return if (buffer != null) {
      val jsonObject: JsonObject = buffer.toJsonObject()
      if (jsonObject.isEmpty) null
      else jsonObject.mapTo(GithubRelease::class.java)
    } else {
      null
    }
  }
}
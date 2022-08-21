package `fun`.hydd.cdda.item_browser_be.util

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlin.streams.toList

/**
 * recursive sort jsonObject key-value by key, no parameter modification
 * @param jsonObject pending sort JsonObject
 * @return sored JsonObject
 */
fun sortJsonObject(jsonObject: JsonObject): JsonObject {
  val sortedMap = LinkedHashMap<String, Any>(jsonObject.size())
  for (entity in jsonObject.map.entries.sortedBy { it.key }) {
    val value = entity.value
    sortedMap[entity.key] = when (value) {
      is JsonObject -> sortJsonObject(value)
      is JsonArray -> sortJsonArray(value)
      else -> value
    }
  }
  return JsonObject(sortedMap)
}

/**
 * recursive sort jsonArray's JsonObject key-value, detail: [sortJsonObject]
 * @param jsonArray pending sort JsonArray
 * @return sored JsonArray
 */
fun sortJsonArray(jsonArray: JsonArray): JsonArray {
  return JsonArray(
    jsonArray.toList().stream().map {
      when (it) {
        is JsonObject -> sortJsonObject(it)
        is JsonArray -> sortJsonArray(it)
        else -> it
      }
    }.toList()
  )
}

/**
 * get length 64 sha256 hash code
 * @receiver JsonObject
 * @return String hash code, length 64
 */
fun JsonObject.getHashString(): String {
  return getStringHash(sortJsonObject(this).toString())
}

/**
 * get length 64 sha256 hash code
 * @receiver JsonArray
 * @return String's hash code, length 64
 */
fun JsonArray.getHashString(): String {
  return getStringHash(sortJsonArray(this).toString())
}


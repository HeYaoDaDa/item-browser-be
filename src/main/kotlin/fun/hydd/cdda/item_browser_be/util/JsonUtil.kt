package `fun`.hydd.cdda.item_browser_be.util

import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_ABSTRACT
import `fun`.hydd.cdda.item_browser_be.model.CddaJson
import `fun`.hydd.cdda.item_browser_be.model.DamageUnit
import `fun`.hydd.cdda.item_browser_be.model.GettextString
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import org.slf4j.LoggerFactory
import java.lang.instrument.IllegalClassFormatException

val log = LoggerFactory.getLogger("fun.hydd.cdda.item_browser_be.util.JsonUtil")

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

private fun <T> getList(jsonObject: JsonObject, key: String, cla: Class<T>, def: List<T>? = null): List<T>? {
  return jsonObject.getJsonArray(key)?.mapNotNull {
    @Suppress("UNCHECKED_CAST") if (it::class.java == cla) it as T
    else throw IllegalArgumentException("$it class not is $cla")
  } ?: def
}

fun JsonObject.getStringList(key: String, def: List<String>? = null): List<String>? {
  return getList(this, key, String::class.java, def)
}

fun JsonObject.getIntList(key: String, def: List<Int>? = null): List<Int>? {
  val jsonArray = this.getJsonArray(key)
  return jsonArray?.map { if (it is Integer) it.toInt() else throw IllegalClassFormatException("$it not is Integer") }
    ?: def
}

fun JsonObject.getDoubleList(key: String, def: List<Double>? = null): List<Double>? {
  val jsonArray = this.getJsonArray(key)
  return jsonArray?.map {
    when (it) {
      is java.lang.Double -> it.toDouble()
      is java.lang.Float -> it.toDouble()
      is Integer -> it.toDouble()
      else -> throw IllegalClassFormatException("$it not is number")
    }
  } ?: def
}

fun JsonObject.getBooleanList(key: String, def: List<Boolean>? = null): List<Boolean>? {
  return getList(this,
    key,
    java.lang.Boolean::class.java,
    def?.map { java.lang.Boolean(it) })?.map { it.booleanValue() }
}

fun JsonObject.getDamageUnitList(key: String, def: List<DamageUnit>? = null): List<DamageUnit>? {
  return this.getJsonArray(key)?.mapNotNull {
    if (it is JsonObject) DamageUnit(it)
    else throw IllegalArgumentException("$it class not is JsonObject")
  } ?: def
}

fun JsonObject.getGettextString(key: String, ctxt: String? = null): GettextString? {
  val value = this.get<Any?>(key)
  return if (value != null) {
    when (value) {
      is String -> GettextString(value, ctxt)
      is JsonObject -> GettextString(value, ctxt)
      else -> throw IllegalClassFormatException("json $value's class not is jsonObject or String")
    }
  } else null
}

fun JsonObject.getDamageUnit(key: String): DamageUnit? {
  val value = this.getJsonObject(key)
  return if (value != null) DamageUnit(value) else null
}

fun getCddaItemId(cddaJson: CddaJson, idFiled: String = "id"): List<String> {
  val json = cddaJson.json
  if (cddaJson.isAbstract) return listOf(json.getString(JSON_KEY_ABSTRACT))
  else {
    if (json.containsKey(idFiled)) {
      return when (val idValue = json.getValue(idFiled)) {
        is String -> {
          listOf(idValue)
        }

        is JsonArray -> {
          idValue.mapNotNull { if (it is String) it else null }
        }

        else -> {
          throw Throwable("id filed $idFiled's value $idValue is not string or string list!")
        }
      }
    } else {
      throw Throwable("$json not contains id field $idFiled!")
    }
  }
}
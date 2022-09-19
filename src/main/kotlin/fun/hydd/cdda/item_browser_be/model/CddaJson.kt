package `fun`.hydd.cdda.item_browser_be.model

import `fun`.hydd.cdda.item_browser_be.constant.JSON_EKY_TYPE
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_ABSTRACT
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_COPY_FROM
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_DELETE
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_EXTEND
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_PROPORTIONAL
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_RELATIVE
import `fun`.hydd.cdda.item_browser_be.util.getBooleanList
import `fun`.hydd.cdda.item_browser_be.util.getDamageUnitList
import `fun`.hydd.cdda.item_browser_be.util.getDoubleList
import `fun`.hydd.cdda.item_browser_be.util.getGettextString
import `fun`.hydd.cdda.item_browser_be.util.getStringList
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * cdda json model
 * @property type json type
 * @property mod mod
 * @property path relative path
 * @property json original JsonObject
 * @constructor
 */
data class CddaJson(
  val mod: CddaMod,
  val path: String,
  val json: JsonObject,
) {
  val type: String
  val copyFromId: String?
  val isAbstract: Boolean
  private val relative: JsonObject?
  private val proportional: JsonObject?
  private val extend: JsonObject?
  private val delete: JsonObject?
  private val log = LoggerFactory.getLogger(CddaJson::class.java)

  /**
   * read json, but json must contain type field.
   */
  init {
    type = json.getString(JSON_EKY_TYPE)
    copyFromId = if (json.containsKey(JSON_KEY_COPY_FROM)) json.getString(JSON_KEY_COPY_FROM) else null
    isAbstract = json.containsKey(JSON_KEY_ABSTRACT)
    relative =
      if (copyFromId != null && json.containsKey(JSON_KEY_RELATIVE)) json.getJsonObject(JSON_KEY_RELATIVE) else null
    proportional =
      if (copyFromId != null && json.containsKey(JSON_KEY_PROPORTIONAL)) json.getJsonObject(JSON_KEY_PROPORTIONAL) else null
    extend = if (copyFromId != null && json.containsKey(JSON_KEY_EXTEND)) json.getJsonObject(JSON_KEY_EXTEND) else null
    delete = if (copyFromId != null && json.containsKey(JSON_KEY_DELETE)) json.getJsonObject(JSON_KEY_DELETE) else null
  }

  fun getValue(key: String, parentValue: Any?): Any? {
    return if (json.containsKey(key)) json.getValue(key)
    else parentValue
  }

  fun getValue(key: String, parentValue: Any?, def: Any): Any {
    return getValue(key, parentValue) ?: def
  }

  fun getString(key: String, parentValue: String?): String? {
    return if (json.containsKey(key)) json.getString(key)
    else parentValue
  }

  fun getString(key: String, parentValue: String?, def: String): String {
    return getString(key, parentValue) ?: def
  }

  fun getBoolean(key: String, parentValue: Boolean?): Boolean? {
    return if (json.containsKey(key)) json.getBoolean(key)
    else parentValue
  }

  fun getBoolean(key: String, parentValue: Boolean?, def: Boolean): Boolean {
    return getBoolean(key, parentValue) ?: def
  }

  fun getDouble(key: String, parentValue: Double?): Double? {
    val result = if (json.containsKey(key)) json.getDouble(key) as Double
    else parentValue
    return if (result != null) processDoubleProportionalAndRelative(result, key)
    else null
  }

  fun getDouble(key: String, parentValue: Double?, def: Double): Double {
    val result = if (json.containsKey(key)) json.getDouble(key) as Double
    else parentValue ?: def
    return processDoubleProportionalAndRelative(result, key)
  }

  fun getGettextString(key: String, ctxt: String? = null, parentValue: GettextString?): GettextString? {
    return if (json.containsKey(key)) json.getGettextString(key, ctxt)
    else parentValue
  }

  @Suppress("UNCHECKED_CAST")
  fun <T> getList(key: String, parentValue: List<T>?): List<T>? {
    val result = if (json.containsKey(key)) json.getJsonArray(key).map { it as T }
    else parentValue
    return result
  }

  fun getStringList(key: String, parentValue: List<String>?, def: List<String>? = null): List<String>? {
    var result = if (json.containsKey(key)) json.getStringList(key)?.toMutableList()
    else (parentValue ?: def)?.toMutableList()
    if (extend != null) {
      val extendValue = extend.getStringList(key)
      if (extendValue != null) {
        if (result != null) result.addAll(extendValue)
        else result = extendValue.toMutableList()
      }
    }
    if (result != null) {
      if (delete != null) {
        val deleteValue = delete.getStringList(key)
        if (deleteValue != null) result.removeAll(deleteValue)
      }
    }
    return result
  }

  fun getBooleanList(key: String, parentValue: List<Boolean>?, def: List<Boolean>? = null): List<Boolean>? {
    var result = if (json.containsKey(key)) json.getBooleanList(key)?.toMutableList()
    else (parentValue ?: def)?.toMutableList()
    if (extend != null) {
      val extendValue = extend.getBooleanList(key)
      if (extendValue != null) {
        if (result != null) result.addAll(extendValue)
        else result = extendValue.toMutableList()
      }
    }
    if (result != null) {
      if (delete != null) {
        val deleteValue = delete.getBooleanList(key)
        if (deleteValue != null) result.removeAll(deleteValue)
      }
    }
    return result
  }

  fun getDoubleList(key: String, parentValue: List<Double>?, def: List<Double>? = null): List<Double>? {
    var result = if (json.containsKey(key)) json.getDoubleList(key)?.toMutableList()
    else (parentValue ?: def)?.toMutableList()
    if (extend != null) {
      val extendValue = extend.getDoubleList(key)
      if (extendValue != null) {
        if (result != null) result.addAll(extendValue)
        else result = extendValue.toMutableList()
      }
    }
    if (result != null) {
      if (delete != null) {
        val deleteValue = delete.getDoubleList(key)
        if (deleteValue != null) result.removeAll(deleteValue)
      }
    }
    return result
  }

  fun getDamageUnitList(key: String, parentValue: List<DamageUnit>?, def: List<DamageUnit>? = null): List<DamageUnit>? {
    val result = if (json.containsKey(key)) json.getDamageUnitList(key)?.toMutableList()
    else (parentValue ?: def)?.toMutableList()
    if (result != null) {
      if (relative != null && relative.containsKey(key)) {
        val relativeValue: Any = relative.getValue(key)
        val relativeDamageUnitList = mutableListOf<DamageUnit>()
        when (relativeValue) {
          is JsonObject -> relativeDamageUnitList.add(DamageUnit(relativeValue))
          is JsonArray -> relativeDamageUnitList.addAll(relative.getDamageUnitList(key) as List<DamageUnit>)
          is Int -> result.forEach { it.amount += relativeValue }
        }
        if (relativeDamageUnitList.isNotEmpty()) {
          relativeDamageUnitList.forEach { relativeDamageUnit ->
            val matchDamageUnit = result.find { it.damageType == relativeDamageUnit.damageType }
            if (matchDamageUnit != null) processDamageUnitRelative(matchDamageUnit, relativeDamageUnit)
          }
        }
      }
      if (proportional != null && proportional.containsKey(key)) {
        val proportionalValue: Any = proportional.getValue(key)
        val proportionalDamageUnitList = mutableListOf<DamageUnit>()
        when (proportionalValue) {
          is JsonObject -> proportionalDamageUnitList.add(DamageUnit(proportionalValue))
          is JsonArray -> proportionalDamageUnitList.addAll(proportional.getDamageUnitList(key) as List<DamageUnit>)
          is Int -> result.forEach { it.amount *= proportionalValue }
        }
        if (proportionalDamageUnitList.isNotEmpty()) {
          proportionalDamageUnitList.forEach { proportionalDamageUnit ->
            val matchDamageUnit = result.find { it.damageType == proportionalDamageUnit.damageType }
            if (matchDamageUnit != null) processDamageUnitProportional(matchDamageUnit, proportionalDamageUnit)
          }
        }
      }
    }
    return result
  }

  private fun processDoubleProportionalAndRelative(value: Double, key: String): Double {
    var result = value
    if (proportional != null) {
      val proportionalValue = proportional.getDouble(key, 1.0)
      if (proportionalValue != null) {
        result = processProportion(result, proportionalValue, key)
      }
    }
    if (relative != null) {
      val relativeValue = relative.getDouble(key, 0.0)
      if (relativeValue != null) {
        result += relativeValue
      }
    }
    return result
  }

  private fun processDamageUnitRelative(value: DamageUnit, relativeValue: DamageUnit) {
    if (value.damageType != relativeValue.damageType) {
      throw IllegalArgumentException("value.damageType ${value.damageType} not is ${relativeValue.damageType}!")
    }
    value.amount += relativeValue.amount
    value.armorPen += relativeValue.armorPen
    value.damageMul += relativeValue.damageMul
    value.armorMul += relativeValue.armorMul
    value.unChangeArmorMul += relativeValue.unChangeArmorMul
    value.unChangeDamageMul += relativeValue.unChangeDamageMul
  }

  private fun processDamageUnitProportional(value: DamageUnit, proportionalValue: DamageUnit) {
    if (value.damageType != proportionalValue.damageType) {
      throw IllegalArgumentException("value.damageType ${value.damageType} not is ${proportionalValue.damageType}!")
    }
    value.amount = processProportion(value.amount, proportionalValue.amount, "amount")
    value.armorPen = processProportion(value.armorPen, proportionalValue.armorPen, "armor_penetration")
    value.armorMul = processProportion(value.armorMul, proportionalValue.armorMul, "armor_multiplier")
    value.damageMul = processProportion(value.damageMul, proportionalValue.damageMul, "damage_multiplier")
    value.unChangeArmorMul = processProportion(
      value.unChangeArmorMul, proportionalValue.unChangeArmorMul, "constant_armor_multiplier"
    )
    value.unChangeDamageMul = processProportion(
      value.unChangeDamageMul, proportionalValue.unChangeDamageMul, "constant_damage_multiplier"
    )
  }

  private fun processProportion(oldValue: Double, newValue: Double, key: String): Double {
    return if (validateProportionalValue(newValue, key)) oldValue * newValue else oldValue
  }

  private fun validateProportionalValue(proportionalValue: Double, key: String): Boolean {
    if (proportionalValue < 0 || proportionalValue > 1) {
      log.warn("${mod.id}:$path's $json : $proportional's $key value $proportionalValue not in 0-1!")
      return false
    }
    return true
  }
}

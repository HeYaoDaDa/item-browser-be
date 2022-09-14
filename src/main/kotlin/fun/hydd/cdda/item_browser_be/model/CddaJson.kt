package `fun`.hydd.cdda.item_browser_be.model

import `fun`.hydd.cdda.item_browser_be.constant.JSON_EKY_TYPE
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_ABSTRACT
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_COPY_FROM
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_DELETE
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_EXTEND
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_PROPORTIONAL
import `fun`.hydd.cdda.item_browser_be.constant.JSON_KEY_RELATIVE
import `fun`.hydd.cdda.item_browser_be.util.getDamageUnit
import `fun`.hydd.cdda.item_browser_be.util.getGettextString
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

  fun getValue(key: String): Any? {
    return json.getValue(key)
  }

  fun getValue(key: String, def: Any): Any {
    return json.getValue(key, def)
  }

  fun getString(key: String): Any? {
    return json.getString(key)
  }

  fun getString(key: String, def: String): String {
    return json.getString(key, def)
  }

  fun getBoolean(key: String): Boolean? {
    return json.getBoolean(key)
  }

  fun getBoolean(key: String, def: Boolean): Boolean {
    return json.getBoolean(key, def)
  }

  fun getDouble(key: String): Double? {
    var result = json.getDouble(key)
    if (result != null) {
      result = processDoubleProportinalAndRelative(result, key)
    }
    return result
  }

  fun getDouble(key: String, def: Double): Double {
    var result = json.getDouble(key, def)
    result = processDoubleProportinalAndRelative(result, key)
    return result
  }

  fun getGettextString(key: String, ctxt: String? = null): GettextString? {
    return json.getGettextString(key, ctxt)
  }

  fun getDamageUnit(key: String): DamageUnit? {
    var damageUnit = json.getDamageUnit(key)
    if (damageUnit != null) {
      damageUnit = processDamageUnitProportional(damageUnit, key)
      damageUnit = processDamageUnitRelative(damageUnit, key)
    }
    return damageUnit
  }

  fun <T> getList(key: String): List<T>? {
    return json.getJsonArray(key)?.mapNotNull { it as T }
  }

  private fun processDoubleProportinalAndRelative(value: Double, key: String): Double {
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

  private fun processDamageUnitRelative(value: DamageUnit, key: String): DamageUnit {
    if (relative != null) {
      val relativeValue = relative.getDamageUnit(key)
      if (relativeValue != null) {
        value.amount += relativeValue.amount
        value.armorPen += relativeValue.armorPen
        value.damageMul += relativeValue.damageMul
        value.armorMul += relativeValue.armorMul
        value.unChangeArmorMul += relativeValue.unChangeArmorMul
        value.unChangeDamageMul += relativeValue.unChangeDamageMul
      }
    }
    return value
  }

  private fun processDamageUnitProportional(value: DamageUnit, key: String): DamageUnit {
    if (proportional != null) {
      val proportionalValue = proportional.getDamageUnit(key)
      if (proportionalValue != null) {
        if (value.damageType != proportionalValue.damageType) {
          throw Throwable("value.damageType ${value.damageType} not is ${proportionalValue.damageType}!")
        }
        value.amount = processProportion(value.amount, proportionalValue.amount, "$key.amount")
        value.armorPen = processProportion(value.armorPen, proportionalValue.armorPen, "$key.armor_penetration")
        value.armorMul = processProportion(value.armorMul, proportionalValue.armorMul, "$key.armor_multiplier")
        value.damageMul = processProportion(value.damageMul, proportionalValue.damageMul, "$key.damage_multiplier")
        value.unChangeArmorMul = processProportion(
          value.unChangeArmorMul, proportionalValue.unChangeArmorMul, "$key.constant_armor_multiplier"
        )
        value.unChangeDamageMul = processProportion(
          value.unChangeDamageMul, proportionalValue.unChangeDamageMul, "$key.constant_damage_multiplier"
        )
      }
    }
    return value
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

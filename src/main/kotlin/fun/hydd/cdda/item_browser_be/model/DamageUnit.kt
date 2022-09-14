package `fun`.hydd.cdda.item_browser_be.model

import `fun`.hydd.cdda.item_browser_be.constant.DamageType
import io.vertx.core.json.JsonObject

class DamageUnit {
  val damageType: DamageType
  var amount: Double = 0.0
  var armorPen: Double = 0.0
  var armorMul: Double = 1.0
  var damageMul: Double = 1.0
  var unChangeArmorMul: Double = 1.0
  var unChangeDamageMul: Double = 1.0

  constructor(input: JsonObject) {
    if (input.containsKey("damage_type")) {
      damageType = DamageType.valueOf(input.getString("damage_type"))
    } else {
      throw Throwable("damageUnit input $input not contain key damage_type")
    }
    amount = input.getDouble("amount", 0.0)
    armorPen = input.getDouble("armor_penetration", 0.0)
    armorMul = input.getDouble("armor_multiplier", 1.0)
    damageMul = input.getDouble("damage_multiplier", 1.0)
    unChangeArmorMul = input.getDouble("constant_armor_multiplier", 1.0)
    unChangeDamageMul = input.getDouble("constant_damage_multiplier", 1.0)
  }
}
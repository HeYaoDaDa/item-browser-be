package `fun`.hydd.cdda.item_browser_be.model

import `fun`.hydd.cdda.item_browser_be.constant.CddaItemType

class CddaItemProduct<T>(val cddaJson: CddaJson, val type: CddaItemType, val id: String, val product: T) {
  val mod by cddaJson::mod
  val path by cddaJson::mod
  val json by cddaJson::json
  val jsonType by cddaJson::type
  val copyFromId by cddaJson::copyFromId
  val isAbstract by cddaJson::isAbstract

  // is may replace pre mod's item
  var isReplace: Boolean = false

  var name: String? = null
  var describe: String? = null
  val premodIds: List<String> = mutableListOf()
}
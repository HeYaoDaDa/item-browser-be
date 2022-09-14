package `fun`.hydd.cdda.item_browser_be.service

import `fun`.hydd.cdda.item_browser_be.constant.CddaItemType
import `fun`.hydd.cdda.item_browser_be.model.CddaItemProduct
import `fun`.hydd.cdda.item_browser_be.model.CddaMod

class CddaItemService(soredMods: List<CddaMod>) {
  // modId.itemType.itemId
  val processedMaps: Map<String, MutableMap<CddaItemType, MutableMap<String, MutableList<CddaItemProduct<Any>>>>> =
    soredMods.associate { it.id to mutableMapOf() }

  // itemType.itemId
  val deferredMap: Map<String, MutableMap<CddaItemType, MutableMap<String, CddaItemProduct<Any>>>> =
    soredMods.associate { it.id to mutableMapOf() }
}
package `fun`.hydd.cdda.item_browser_be.model.handler

import `fun`.hydd.cdda.item_browser_be.constant.CddaItemType
import `fun`.hydd.cdda.item_browser_be.model.CddaItemProduct
import `fun`.hydd.cdda.item_browser_be.model.CddaJson
import `fun`.hydd.cdda.item_browser_be.model.GettextString
import `fun`.hydd.cdda.item_browser_be.util.getCddaItemId

class ModInfoHandler : CddaItemHandler<ModInfo> {
  override fun preHanding(cddaJson: CddaJson): List<CddaItemProduct<ModInfo>> {
    return getCddaItemId(cddaJson).map { id ->
      CddaItemProduct(cddaJson, CddaItemType.MOD_INFO, id, ModInfo())
    }
  }

  override fun handing(cddaItemProduct: CddaItemProduct<ModInfo>): Pair<CddaItemType, String>? {
    TODO("Not yet implemented")
  }
}

data class ModInfo(
  var name: GettextString? = null,
  var description: GettextString? = null,
  var version: String? = null,
  var path: String? = null,
  var authors: List<String>? = null,
  var maintainers: List<String>? = null,
  var dependencies: List<String>? = null,
  var isCore: Boolean = false,
  var isObsolete: Boolean = false
)
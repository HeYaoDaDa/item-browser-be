package `fun`.hydd.cdda.item_browser_be.model.handler

import `fun`.hydd.cdda.item_browser_be.constant.CddaItemType
import `fun`.hydd.cdda.item_browser_be.model.CddaItemProduct
import `fun`.hydd.cdda.item_browser_be.model.CddaJson

interface CddaItemHandler<T> {
  fun preHanding(cddaJson: CddaJson): List<CddaItemProduct<T>>
  fun handing(cddaItemProduct: CddaItemProduct<T>): Pair<CddaItemType, String>?
}
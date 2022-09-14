package `fun`.hydd.cdda.item_browser_be.model

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * cdda mod class
 * @property id String
 * @property name String
 * @property depIds List<String>
 * @property depMods List<CddaMod> It needs to be set uniformly after sorting.
 * @property allDepModSet Set<CddaMod> depend [depMods]
 * @property allDepModIdSet Set<String> depend [depMods]
 * @constructor
 */
data class CddaMod(
  val id: String,
  val name: String,
  val depIds: List<String>,
  val depMods: MutableSet<CddaMod> = mutableSetOf(),
  val isObsolete: Boolean = false
) {
  private val allDepModSet: Set<CddaMod> by lazy {
    depMods.flatMap { it.allDepModSet + it }.toHashSet()
  }
  private val allDepModIdSet: Set<String> by lazy {
    allDepModSet.map { it.id }.toHashSet()
  }

  /**
   * return mod is allowing load by after
   * @param modId String
   * @return Boolean
   */
  fun allowAfter(modId: String): Boolean {
    if (id == modId || modId == "dda") {
      return false
    }
    return !allDepModIdSet.contains(modId)
  }
}

/**
 * from jsonObject construct CddaMod
 * @param jsonObject JsonObject
 * @return CddaMod
 */
fun cddaModOf(jsonObject: JsonObject): CddaMod {
  return CddaMod(jsonObject.getString("id"),
    jsonObject.getString("name"),
    jsonObject.getJsonArray("dependencies", JsonArray()).mapNotNull { if (it is String) it else null },
    isObsolete = jsonObject.getBoolean("obsolete", false)
  )
}

/**
 * topology Sort by dependent modId, no change param
 * @param mods List<CddaMod>
 * @return List<CddaMod>
 */
fun sortMods(mods: List<CddaMod>): List<CddaMod> {
  val resultModIds = mutableListOf<String>();
  val myMods = mods.map { Pair(it.id, it.depIds.toMutableList()) }.toMutableList()
  while (myMods.isNotEmpty()) {
    val beforeSize = myMods.size
    myMods.filter { it.second.isEmpty() }.map { resultModIds.add(it.first);myMods.remove(it) }
    myMods.map { it.second.removeIf { depModId -> resultModIds.contains(depModId) } }
    if (myMods.size == beforeSize) {
      break
    }
  }
  return resultModIds.map { modId -> mods.first { it.id == modId } }
}

/**
 * set mods depMods, mods must sored
 * @param soredMods List<CddaMod>
 */
fun setAllModDepMods(soredMods: List<CddaMod>) {
  val modMap = soredMods.associateBy { it.id }
  soredMods.forEach { superMod -> superMod.depMods.addAll(superMod.depIds.mapNotNull { modMap[it] }) }
}
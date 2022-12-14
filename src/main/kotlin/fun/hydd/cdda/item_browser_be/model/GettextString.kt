package `fun`.hydd.cdda.item_browser_be.model

import io.vertx.core.json.JsonObject
import java.lang.instrument.IllegalClassFormatException

class GettextString {
  val value: String
  val ctxt: String?

  constructor(value: String, ctxt: String? = null) {
    this.value = value
    this.ctxt = ctxt
  }

  constructor(input: JsonObject, ctxt: String? = null) {
    var lastCtxt = ctxt
    var isMale = false
    var isFemale = false
    if (input.containsKey("str")) {
      this.value = input.getString("str")
    } else if (input.containsKey("str_sp")) {
      this.value = input.getString("str_sp")
    } else if (input.containsKey("male")) {
      this.value = input.getString("male")
      isMale = true
    } else if (input.containsKey("female")) {
      this.value = input.getString("female")
      isFemale = true
    } else {
      throw IllegalClassFormatException("GettextString input json \n$input\nno have str, str_sp, male or female!")
    }
    lastCtxt = getLastCtxtByJsonObject(input, lastCtxt, isMale, isFemale)
    this.ctxt = lastCtxt
  }

  private fun getLastCtxtByJsonObject(
    input: JsonObject, lastCtxt: String?, isMale: Boolean, isFemale: Boolean
  ): String? {
    var lastCtxt1 = lastCtxt
    if (input.containsKey("ctxt")) {
      val jsonCtxt = input.getString("ctxt")
      if (lastCtxt1 != null) throw IllegalArgumentException("input ctxt $lastCtxt1, but has json ctxt $jsonCtxt")
      else lastCtxt1 = jsonCtxt
    }
    if (lastCtxt1 != null) {
      if (isMale) lastCtxt1 += "_male"
      if (isFemale) lastCtxt1 += "_female"
    }
    return lastCtxt1
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GettextString

    if (value != other.value) return false
    if (ctxt != other.ctxt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = value.hashCode()
    result = 31 * result + (ctxt?.hashCode() ?: 0)
    return result
  }


}

package `fun`.hydd.cdda.item_browser_be.model

import io.vertx.core.json.JsonObject

class GettextString {
  var value: String
  var ctxt: String? = null

  constructor(value: String, ctxt: String? = null) {
    this.value = value
    this.ctxt = ctxt
  }

  constructor(input: Any, ctxt: String? = null) {
    this.ctxt = ctxt
    if (input is String) {
      this.value = input
    } else if (input is JsonObject) {
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
        throw Throwable("GettextString input json \n$input\nno have str, str_sp, male or female!")
      }
      if (this.ctxt != null) {
        if (isMale) this.ctxt += "_male"
        if (isFemale) this.ctxt += "_female"
      }
    } else {
      throw Throwable("GettextString input class ${input::class.java} is Wrong!")
    }
  }
}

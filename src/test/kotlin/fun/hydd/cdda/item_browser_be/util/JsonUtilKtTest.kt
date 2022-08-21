package `fun`.hydd.cdda.item_browser_be.util

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class JsonUtilKtTest {
  private var json1 = JsonObject()
  private var jsonArray1 = JsonArray()

  @BeforeEach
  fun initJson() {
    json1 = json {
      obj(
        "e" to 6, "a" to 1, "d" to 5, "h" to array("b", "a", json { obj("3" to 3, "a" to 1, "c" to 3, "1" to 0) }, "z"),
        "b" to 2, "f" to 7, "c" to json {
          obj(
            "e" to 6, "a" to 1, "d" to 5, "h" to 9, "b" to 2, "f" to 7, "c" to 4, "g" to 8
          )
        }, "g" to 8
      )
    }
    jsonArray1 = JsonArray()
    jsonArray1.add("b")
    jsonArray1.add(1)
    jsonArray1.add(3)
    jsonArray1.add(json1)
    jsonArray1.add(4)
  }

  @Test
  fun sortJsonObjectTest() {
    Assertions.assertEquals(
      "{\"a\":1,\"b\":2,\"c\":{\"a\":1,\"b\":2,\"c\":4,\"d\":5,\"e\":6,\"f\":7,\"g\":8,\"h\":9},\"d\":5,\"e\":6,\"f\":7,\"g\":8,\"h\":[\"b\",\"a\",{\"1\":0,\"3\":3,\"a\":1,\"c\":3},\"z\"]}",
      sortJsonObject(json1).toString()
    )
  }

  @Test
  fun sortJsonArrayTest() {
    Assertions.assertEquals(
      "[\"b\",1,3,{\"a\":1,\"b\":2,\"c\":{\"a\":1,\"b\":2,\"c\":4,\"d\":5,\"e\":6,\"f\":7,\"g\":8,\"h\":9},\"d\":5,\"e\":6,\"f\":7,\"g\":8,\"h\":[\"b\",\"a\",{\"1\":0,\"3\":3,\"a\":1,\"c\":3},\"z\"]},4]",
      sortJsonArray(jsonArray1).toString()
    )
  }
}
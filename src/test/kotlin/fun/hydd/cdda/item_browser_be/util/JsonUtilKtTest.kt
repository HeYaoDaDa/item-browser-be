package `fun`.hydd.cdda.item_browser_be.util

import `fun`.hydd.cdda.item_browser_be.model.GettextString
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.instrument.IllegalClassFormatException

internal class JsonUtilKtTest {
  private var json1 = JsonObject()
  private var json2 = JsonObject()
  private var json3 = JsonObject()
  private var json4 = JsonObject()
  private var jsonArray1 = JsonArray()

  @BeforeEach
  fun initJson() {
    json1 = json {
      obj(
        "e" to 6,
        "a" to 1,
        "d" to 5,
        "h" to array("b", "a", json { obj("3" to 3, "a" to 1, "c" to 3, "1" to 0) }, "z"),
        "b" to 2,
        "f" to 7,
        "c" to json {
          obj(
            "e" to 6, "a" to 1, "d" to 5, "h" to 9, "b" to 2, "f" to 7, "c" to 4, "g" to 8
          )
        },
        "g" to 8
      )
    }
    json2 = json { obj("a" to array(1, 2, 3, 4), "b" to array("1", "2"), "c" to array(false, true)) }
    json3 = json {
      obj(
        "stringList" to array("string1", "string2"),
        "intList" to array(1, 2),
        "doubleList" to array(1.5, 2.5),
        "booleanList" to array(true, false),
        "doubleAndIntList" to array(1.5, 2, 3)
      )
    }
    json4 = json {
      obj(
        "string" to "stringValue",
        "str" to json { obj("str" to "stringValue") },
        "str&ctxt" to json { obj("str" to "stringValue", "ctxt" to "ctxt") },
        "strSp" to json { obj("str_sp" to "stringValue") },
        "strMale" to json { obj("male" to "stringValue") },
        "thrown" to 1
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

  @Test
  fun getStringListTest() {
    Assertions.assertEquals(listOf("string1", "string2"), json3.getStringList("stringList"))
  }

  @Test
  fun getIntListTest() {
    Assertions.assertEquals(listOf(1, 2), json3.getIntList("intList"))
  }

  @Test
  fun getIntListTestThrow() {
    Assertions.assertThrows(IllegalClassFormatException::class.java) {
      json3.getIntList("doubleList")
    }
  }

  @Test
  fun getDoubleListTest() {
    Assertions.assertEquals(listOf(1.5, 2.5), json3.getDoubleList("doubleList"))
  }

  @Test
  fun getDoubleListTest1() {
    Assertions.assertEquals(listOf(1.5, 2.0, 3.0), json3.getDoubleList("doubleAndIntList"))
  }

  @Test
  fun getBooleanListTest() {
    Assertions.assertEquals(listOf(true, false), json3.getBooleanList("booleanList"))
  }

  @Test
  fun getGettextStringTest() {
    Assertions.assertEquals(GettextString("stringValue"), json4.getGettextString("string"))
  }

  @Test
  fun getGettextStringTest1() {
    Assertions.assertEquals(GettextString("stringValue"), json4.getGettextString("str"))
  }

  @Test
  fun getGettextStringTest2() {
    Assertions.assertEquals(GettextString("stringValue", "ctxt"), json4.getGettextString("str&ctxt"))
  }

  @Test
  fun getGettextStringTest3() {
    Assertions.assertEquals(GettextString("stringValue", "ctxt"), json4.getGettextString("str", "ctxt"))
  }

  @Test
  fun getGettextStringTest4() {
    Assertions.assertEquals(GettextString("stringValue"), json4.getGettextString("strSp"))
  }

  @Test
  fun getGettextStringTest5() {
    Assertions.assertEquals(GettextString("stringValue"), json4.getGettextString("strMale"))
  }

  @Test
  fun getGettextStringTest6() {
    Assertions.assertEquals(GettextString("stringValue", "ctxt_male"), json4.getGettextString("strMale", "ctxt"))
  }

  @Test
  fun getGettextStringThrown() {
    Assertions.assertThrows(IllegalClassFormatException::class.java) {
      json4.getGettextString("thrown")
    }
  }

  @Test
  fun getGettextStringThrown1() {
    Assertions.assertThrows(IllegalArgumentException::class.java) {
      json4.getGettextString("str&ctxt", "ctxt")
    }
  }
}
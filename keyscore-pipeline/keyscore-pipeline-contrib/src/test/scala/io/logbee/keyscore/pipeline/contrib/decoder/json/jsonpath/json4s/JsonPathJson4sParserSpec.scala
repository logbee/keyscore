package io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.json4s

import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.JsonPath
import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.json4s.JsonPathJson4s._
import org.json4s.JsonAST._
import org.json4s.native.JsonParser.parse
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class JsonPathJson4sParserSpec extends FreeSpec with Matchers {

  case class Fixture(title: String, jsonpath: JsonPath, expectation: JValue)

  "A JsonPathJson4sParser" - {

    val json = parse(
      """
        |{
        |  "device": {
        |    "name": "robot",
        |    "id": 1337,
        |    "weight": 73.42,
        |    "logs": [
        |      "Hello World",
        |      "The weather is cloudy!",
        |      "Robot fucked up beyond all recognition!"
        |    ],
        |    "temperatures": [
        |      {
        |        "time": 1,
        |        "temperature": 5,
        |        "sensor": {
        |           "name": "a1"
        |        }
        |      },
        |      {
        |        "time": 3,
        |        "temperature": 7
        |        "sensor": {
        |           "name": "a2"
        |        }
        |      },
        |      {
        |        "time": 7,
        |        "temperature": 15
        |        "sensor": {
        |           "name": "a1"
        |        }
        |      }
        |    ]
        |  },
        |  "hardware": {
        |    "sensors": {
        |       "0": {
        |         "name": "a1",
        |         "id": 3
        |       },
        |       "1": {
        |         "name": "a2",
        |         "id": 35
        |       }
        |    }
        |  }
        |}
      """.stripMargin)

    import JsonPath.String2JsonPath

    Seq(
      Fixture(
        title = "a simple sting value",
        jsonpath = "$.device.name",
        expectation = JString("robot")),

      Fixture(
        title = "a simple int value",
        jsonpath = "$.device.id",
        expectation = JInt(1337)),

      Fixture(
        title = "a simple double value",
        jsonpath = "$.device.weight",
        expectation = JDouble(73.42)),

      Fixture(
        title = "one element of an array",
        jsonpath = "$.device.logs[1:]",
        expectation = JString("The weather is cloudy!")),

      Fixture(
        title = "one element of an array (from right-to-left)",
        jsonpath = "$.device.logs[-1:]",
        expectation = JString("Robot fucked up beyond all recognition!")),

      Fixture(
        title = "multiple elements of an array",
        jsonpath = "$.device.logs[1:2]",
        expectation = JArray(List(
          JString("The weather is cloudy!"),
          JString("Robot fucked up beyond all recognition!")))),

      Fixture(
        title = "multiple elements of an array",
        jsonpath = "$.device.logs[:1]",
        expectation = JArray(List(
          JString("Hello World"),
          JString("The weather is cloudy!")))),

      Fixture(
        title = "all elements of an array",
        jsonpath = "$.device.logs",
        expectation = JArray(List(
          JString("Hello World"),
          JString("The weather is cloudy!"),
          JString("Robot fucked up beyond all recognition!")))),

      Fixture(
        title = "multiple sub-elements of an array",
        jsonpath = "$.device.temperatures[0:1].temperature",
        expectation = JArray(List(
          JInt(5),
          JInt(7)))),

      Fixture(
        title = "multiple sub-sub-elements of an array",
        jsonpath = "$.device.temperatures[0:1].sensor.name",
        expectation = JArray(List(
          JString("a1"),
          JString("a2")))),

      Fixture(
        title = "wildcard elements",
        jsonpath = "$.hardware.sensors.*",
        expectation = JArray(List(
          JObject(
            ("name", JString("a1")),
            ("id", JInt(3))
          ),
          JObject(
            ("name", JString("a2")),
            ("id", JInt(35))
          ),
        ))
      ),

      Fixture(
        title = "wildcard sub-elements",
        jsonpath = "$.hardware.sensors.*.name",
        expectation = JArray(List(JString("a1"), JString("a2")))
      )
    )

    .foreach { case Fixture(title, jsonpath, expectation) =>

      s"should parse $title: ${jsonpath.path}" in {

        jsonpath.parse(json) shouldBe expectation
      }
    }
  }
}

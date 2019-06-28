package io.logbee.keyscore.pipeline.contrib.decoder.json

import io.logbee.keyscore.model.configuration.{BooleanParameter, Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.util.Using
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}
import org.json4s.JsonAST._
import org.json4s.native.JsonParser._

import scala.annotation.tailrec


@RunWith(classOf[JUnitRunner])
class JsonPathJsonExtractorLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A JsonPathJsonExtractorLogic" - {

    val plainJson = Using.using(getClass.getResourceAsStream("/JsonPathJsonExtractorLogic.example.json")) { stream =>
      scala.io.Source.fromInputStream(stream).mkString
    }

    "fubar" in {

//      import JsonPath._

      val json = parse(plainJson)
      val node = "device"

//      println(json \?[JValue] "$.device.temperatures.data[1].time")
    }

//    "when configured to extract 'device.temperatures.data' from rawJson" - {
//
//      val configuration = Configuration(
//        TextParameter("sourceFieldName", "rawJson"),
//        TextParameter("jsonpath", "device.temperatures.data")
//      )
//
//      "should extract the data as recods" in new TestStreamForFilter[JsonPathJsonExtractorLogic](configuration) {
//
//        whenReady(filterFuture) { _ =>
//
//          val sample = Dataset(Record(
//            Field("rawJson", TextValue(plainJson))
//          ))
//
//          sink.request(1)
//          source.sendNext(sample)
//
//          val dataset = sink.requestNext()
//          dataset.records should contain allOf(
//            Record(
//              Field("time", NumberValue(1)),
//              Field("temperature", NumberValue(5)),
//            ),
//            Record(
//              Field("time", NumberValue(3)),
//              Field("temperature", NumberValue(7)),
//            ),
//            Record(
//              Field("time", NumberValue(7)),
//              Field("temperature", NumberValue(15))
//            )
//          )
//        }
//      }
//    }

//    "should decode a json batch into several records" in new TestStreamForFilter[JsonDecoderLogic](configuration) {
//
//      whenReady(filterFuture) { _ =>
//
//        val sample = Dataset(Record(
//          Field("rawJson", TextValue(batchJson))
//        ))
//
//        sink.request(1)
//        source.sendNext(sample)
//
//        val dataset = sink.requestNext()
//
//        dataset.records should have size 3
//
//        dataset.records.foreach( record => {
//
//          record.fields should contain only (
//            Field("message", TextValue("This is an example message.")),
//            Field("device.name", TextValue("robot")),
//            Field("device.vendor", TextValue("kuka")),
//            Field("device.decimal", DecimalValue(73.42)),
//            Field("device.logs.0", TextValue("Hello World")),
//            Field("device.logs.1", TextValue("The weather is cloudy!")),
//            Field("device.logs.2", TextValue("Robot fucked up beyond all recognition!")),
//            Field("device.temperatures.data.0.time", NumberValue(1)),
//            Field("device.temperatures.data.0.temperature", NumberValue(5)),
//            Field("device.temperatures.data.1.time", NumberValue(3)),
//            Field("device.temperatures.data.1.temperature", NumberValue(7)),
//            Field("device.temperatures.data.2.time", NumberValue(7)),
//            Field("device.temperatures.data.2.temperature", NumberValue(15))
//          )
//        })
//      }
//    }
  }
}

package io.logbee.keyscore.pipeline.contrib.decoder.json

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{BooleanParameter, Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.util.Using
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.test.TestStreamFor
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class JsonDecoderLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val configuration = Configuration(
    TextParameter("sourceFieldName", "rawJson"),
    BooleanParameter("removeSourceField", true)
  )

  val plainJson = Using.using(getClass.getResourceAsStream("JsonDecoderLogicSpec.example.json")) { stream =>
    scala.io.Source.fromInputStream(stream).mkString
  }

  val batchJson = Using.using(getClass.getResourceAsStream("JsonDecoderLogicSpec.batch.example.json")) { stream =>
    scala.io.Source.fromInputStream(stream).mkString
  }

  "A JsonDecoderLogic" - {

    "should return a Descriptor" in {
      JsonDecoderLogic.describe should not be null
    }

    "should extract all json values into separate fields" in new TestStreamFor[JsonDecoderLogic](configuration) {

      whenReady(filterFuture) { _ =>

        val sample = Dataset(Record(
          Field("rawJson", TextValue(plainJson))
        ))

        sink.request(1)
        source.sendNext(sample)

        val dataset = sink.requestNext()

        dataset.records.head.fields should contain only (
          Field("message", TextValue("This is an example message.")),
          Field("device.name", TextValue("robot")),
          Field("device.vendor", TextValue("kuka")),
          Field("device.decimal", DecimalValue(73.42)),
          Field("device.logs.0", TextValue("Hello World")),
          Field("device.logs.1", TextValue("The weather is cloudy!")),
          Field("device.logs.2", TextValue("Robot fucked up beyond all recognition!")),
          Field("device.temperatures.data.0.time", NumberValue(1)),
          Field("device.temperatures.data.0.temperature", NumberValue(5)),
          Field("device.temperatures.data.1.time", NumberValue(3)),
          Field("device.temperatures.data.1.temperature", NumberValue(7)),
          Field("device.temperatures.data.2.time", NumberValue(7)),
          Field("device.temperatures.data.2.temperature", NumberValue(15))
        )
      }
    }

    "should decode a json batch into several records" in new TestStreamFor[JsonDecoderLogic](configuration) {

      whenReady(filterFuture) { _ =>

        val sample = Dataset(Record(
          Field("rawJson", TextValue(batchJson))
        ))

        sink.request(1)
        source.sendNext(sample)

        val dataset = sink.requestNext()

        dataset.records should have size 3

        dataset.records.foreach( record => {

          record.fields should contain only (
            Field("message", TextValue("This is an example message.")),
            Field("device.name", TextValue("robot")),
            Field("device.vendor", TextValue("kuka")),
            Field("device.decimal", DecimalValue(73.42)),
            Field("device.logs.0", TextValue("Hello World")),
            Field("device.logs.1", TextValue("The weather is cloudy!")),
            Field("device.logs.2", TextValue("Robot fucked up beyond all recognition!")),
            Field("device.temperatures.data.0.time", NumberValue(1)),
            Field("device.temperatures.data.0.temperature", NumberValue(5)),
            Field("device.temperatures.data.1.time", NumberValue(3)),
            Field("device.temperatures.data.1.temperature", NumberValue(7)),
            Field("device.temperatures.data.2.time", NumberValue(7)),
            Field("device.temperatures.data.2.temperature", NumberValue(15))
          )
        })
      }
    }
  }
}

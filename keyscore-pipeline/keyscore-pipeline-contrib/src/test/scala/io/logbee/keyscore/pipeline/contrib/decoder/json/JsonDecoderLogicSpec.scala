package io.logbee.keyscore.pipeline.contrib.decoder.json

import io.logbee.keyscore.model.configuration.{BooleanParameter, Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.util.Using
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonDecoderLogicSpec extends AnyFreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val configuration = Configuration(
    TextParameter("sourceFieldName", "rawJson"),
    BooleanParameter("removeSourceField", true)
  )

  val configurationWithDelimiter = Configuration(
    TextParameter("sourceFieldName", "rawJson"),
    BooleanParameter("removeSourceField", true),
    TextParameter("delimiter", "_1F595_")
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

    "should extract all json values into separate fields" in new TestStreamForFilter[JsonDecoderLogic](configuration) {

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

    "should extract all json into separate fields with the configured delimiter" in new TestStreamForFilter[JsonDecoderLogic](configurationWithDelimiter) {

      whenReady(filterFuture) { _ =>

        val sample = Dataset(Record(
          Field("rawJson", TextValue(plainJson))
        ))

        sink.request(1)
        source.sendNext(sample)

        val dataset = sink.requestNext()

        dataset.records.head.fields should contain only (
          Field("message", TextValue("This is an example message.")),
          Field("device_1F595_name", TextValue("robot")),
          Field("device_1F595_vendor", TextValue("kuka")),
          Field("device_1F595_decimal", DecimalValue(73.42)),
          Field("device_1F595_logs_1F595_0", TextValue("Hello World")),
          Field("device_1F595_logs_1F595_1", TextValue("The weather is cloudy!")),
          Field("device_1F595_logs_1F595_2", TextValue("Robot fucked up beyond all recognition!")),
          Field("device_1F595_temperatures_1F595_data_1F595_0_1F595_time", NumberValue(1)),
          Field("device_1F595_temperatures_1F595_data_1F595_0_1F595_temperature", NumberValue(5)),
          Field("device_1F595_temperatures_1F595_data_1F595_1_1F595_time", NumberValue(3)),
          Field("device_1F595_temperatures_1F595_data_1F595_1_1F595_temperature", NumberValue(7)),
          Field("device_1F595_temperatures_1F595_data_1F595_2_1F595_time", NumberValue(7)),
          Field("device_1F595_temperatures_1F595_data_1F595_2_1F595_temperature", NumberValue(15))
        )
      }
    }

    "should decode a json batch into several records" in new TestStreamForFilter[JsonDecoderLogic](configuration) {

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

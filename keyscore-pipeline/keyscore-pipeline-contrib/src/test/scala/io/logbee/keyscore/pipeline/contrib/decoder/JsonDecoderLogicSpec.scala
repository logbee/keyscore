package io.logbee.keyscore.pipeline.contrib.decoder

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{BooleanParameter, Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.util.Using
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
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

  val context = StageContext(system, executionContext)
  val filterStage = new FilterStage(LogicParameters(randomUUID(), context, configuration), (p: LogicParameters, s: FlowShape[Dataset, Dataset]) => new JsonDecoderLogic(p, s))

  val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
    .viaMat(filterStage)(Keep.both)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()

  val json = Using.using(getClass.getResourceAsStream("/JsonExtractorLogicSpec.example.json")) { stream =>
    scala.io.Source.fromInputStream(stream).mkString
  }

  val sample = Dataset(Record(
    Field("rawJson", TextValue(json))
  ))

  "A JsonExtractor" - {

    "should return a MetaFilterDescriptor" in {
      JsonDecoderLogic.describe should not be null
    }

    "should extract all json values into separate fields" in {

      whenReady(filterFuture) { filter =>

        sink.request(1)
        source.sendNext(sample)

        val dataset = sink.requestNext()

        dataset.records.head.fields should contain only (
          Field("message", TextValue("This is an example message.")),
          Field("device.name", TextValue("robot")),
          Field("device.vendor", TextValue("kuka")),
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
  }
}

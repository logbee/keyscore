package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.commons.util.Using
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class JsonExtractorLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val configuration = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(
    TextParameter("sourceFieldName", "rawJson"),
    BooleanParameter("removeSourceField", true)
  ))

  val context = StageContext(system, executionContext)
  val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new JsonExtractorLogic(ctx, c, s))

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
      JsonExtractorLogic.describe should not be null
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

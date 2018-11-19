package io.logbee.keyscore.pipeline.contrib.encoder

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{ChoiceParameter, Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.encoder.JsonEncoderLogic.{KEEP_BATCH, SPLIT_BATCH}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class JsonEncoderLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  class TestStream(strategy: String) {
    val configuration = Configuration(
      TextParameter(JsonEncoderLogic.fieldNameParameter.ref, "output"),
      ChoiceParameter(JsonEncoderLogic.batchStrategyParameter.ref, strategy)
    )

    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(LogicParameters(randomUUID(), context, configuration), (p: LogicParameters, s: FlowShape[Dataset, Dataset]) => new JsonEncoderLogic(p, s))

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val sample = Dataset(
    Record(
      Field("temperature", DecimalValue(11.6)),
      Field("message", TextValue("Hello World!"))
    ),
    Record(
      Field("temperature", DecimalValue(5.3)),
      Field("message", TextValue("Bye Bye"))
    ),
  )

  "A JsonEncoder" - {

    "should encode _all_ records into json" in new TestStream(strategy = KEEP_BATCH) {

      whenReady(filterFuture) { filter =>

        sink.request(1)
        source.sendNext(sample)

        val dataset = sink.requestNext()

        dataset.records should have size 1

        dataset.records.head.fields should contain only (
          Field("output", TextValue(value = """[{"temperature":11.6,"message":"Hello World!"},{"temperature":5.3,"message":"Bye Bye"}]"""))
        )
      }
    }

    "should encode _each_ record into json" in new TestStream(strategy = SPLIT_BATCH) {

      whenReady(filterFuture) { filter =>

        sink.request(1)
        source.sendNext(sample)

        val dataset = sink.requestNext()

        dataset.records should have size 2

        dataset.records(0).fields should contain only (
          Field("output", TextValue(value = """{"temperature":11.6,"message":"Hello World!"}"""))
        )

        dataset.records(1).fields should contain only (
          Field("output", TextValue(value = """{"temperature":5.3,"message":"Bye Bye"}"""))
        )
      }
    }
  }
}

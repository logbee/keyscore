package io.logbee.keyscore.pipeline.contrib.filter

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{Configuration, FieldNameListParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RetainFieldsLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)

    val config = Configuration(
      parameters = Seq(
        FieldNameListParameter(RetainFieldsLogic.fieldNamesParameter.ref, Seq("message", "temperature"))
      )
    )

    val provider = (parameters: LogicParameters, s: FlowShape[Dataset, Dataset]) =>
      new RetainFieldsLogic(LogicParameters(UUID.randomUUID(), context, config), s)

    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, config), provider)

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()

    val sample = Dataset(Record(
      Field("message", TextValue("Hello World!")),
      Field("temperature", DecimalValue(11.5)),
      Field("fubar", TextValue("Delete Me!"))
    ))

    val modifiedSample = Dataset(Record(
      Field("message", TextValue("Hello World!")),
      Field("temperature", DecimalValue(11.5))
    ))
  }

  "A RetainFieldsFilter" should {

    "return a MetaFilterDescriptor" in {
      RetainFieldsLogic.describe should not be null
    }

    "retain only the specified fields and remove all others" in new TestStream {

      whenReady(filterFuture) { filter =>

        source.sendNext(sample)
        sink.request(1)
        sink.expectNext(modifiedSample)
      }
    }
  }
}

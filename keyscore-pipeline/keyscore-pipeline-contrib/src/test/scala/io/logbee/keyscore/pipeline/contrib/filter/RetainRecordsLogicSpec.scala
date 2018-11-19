package io.logbee.keyscore.pipeline.contrib.filter

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{Configuration, FieldNameListParameter}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.filter.DropRecordsLogic.fieldNamesParameter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Inside, Matchers}

@RunWith(classOf[JUnitRunner])
class RetainRecordsLogicSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with Inside with TestSystemWithMaterializerAndExecutionContext  {

  trait TestStream {

    val context = StageContext(system, executionContext)
    val provider = (parameters: LogicParameters, s: FlowShape[Dataset, Dataset]) => new RetainRecordsLogic(parameters, s)

    val fieldNames = FieldNameListParameter(fieldNamesParameter.ref, List("message", "fubar"))
    val initialConfig = Configuration(parameters = Seq(fieldNames))
    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, initialConfig), provider)

    val ((source,filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val sample = Dataset(
    Record(
      Field("message", TextValue("Keep me!")),
      Field("fubar", TextValue("Keep me too!"))
    ),
    Record(
      Field("fubar", TextValue("Drop me!"))
    )
  )

  "A RetainRecordsFilterLogic" - {

    "should only let records pass which contain all of the specified fields" in new TestStream {

      whenReady(filterFuture){ filter =>

        source.sendNext(sample)
        sink.request(1)

        val actual = sink.expectNext()

        actual.records should have size 1

        inside(actual.records.head.fields.head) { case Field("message", TextValue(value)) =>
          value shouldBe "Keep me!"
        }
      }
    }
  }
}

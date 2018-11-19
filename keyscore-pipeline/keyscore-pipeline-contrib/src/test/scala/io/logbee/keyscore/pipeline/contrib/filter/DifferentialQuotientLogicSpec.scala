package io.logbee.keyscore.pipeline.contrib.filter

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.filter.DifferentialQuotientLogic.{targetFieldNameParameter, xFieldNameParameter, yFieldNameParameter}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class DifferentialQuotientLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)

    val configuration = Configuration(parameters = Seq(
      TextParameter(xFieldNameParameter.ref, "time"),
      TextParameter(yFieldNameParameter.ref, "voltage"),
      TextParameter(targetFieldNameParameter.ref, "slope")
    ))

    val provider = (parameters: LogicParameters, s: FlowShape[Dataset,Dataset]) => new DifferentialQuotientLogic(parameters, s)
    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, configuration), provider)
    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val sample1 = Dataset(Record(
    Field("time", NumberValue(1)),
    Field("voltage", DecimalValue(1.0))
  ))

  val sample2 = Dataset(Record(
    Field("time", NumberValue(2)),
    Field("voltage", DecimalValue(4.0))
  ))

  val unwantedSample = Dataset(Record(
    Field("message", TextValue("Hello World!"))
  ))

  "A DifferentialQuotient" - {

    "should let pass datasets unchanged if they do not contain all required fields" in new TestStream {

      sink.request(1)
      source.sendNext(unwantedSample)

      sink.requestNext() shouldBe unwantedSample
    }

    "should set the computed differential quotient to 0 on the first dataset" in new TestStream {

      sink.request(1)
      source.sendNext(sample1)

      val dataset = sink.requestNext()
      val slope = findSlopeField(dataset.records.head)

      slope shouldBe 'defined
      slope.get.toDecimalField.value shouldBe 0
    }

    "should compute the differential quotient of the specified field between two consecutive datasets" in new TestStream {

      sink.request(2)
      source.sendNext(sample1)
      source.sendNext(sample2)

      val dataset1 = sink.requestNext()
      val dataset2 = sink.requestNext()
      var record = dataset2.records.head

      val slope = findSlopeField(record)

      slope shouldBe 'defined
      slope.get.toDecimalField.value shouldBe 3.0
    }
  }

  private def findSlopeField(record: Record) = {
    record.fields.find(field => "slope".eq(field.name))
  }
}

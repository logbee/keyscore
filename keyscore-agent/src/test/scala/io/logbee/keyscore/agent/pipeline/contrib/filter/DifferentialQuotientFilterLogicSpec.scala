package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextMapParameter, TextParameter}
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class DifferentialQuotientFilterLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)
    val configuration = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(
      TextParameter("xFieldName", "time"),
      TextParameter("yFieldName", "voltage"),
      TextParameter("targetFieldName", "slope")
    ))

    val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new DifferentialQuotientFilterLogic(ctx, c, s))

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

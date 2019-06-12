package io.logbee.keyscore.pipeline.contrib.math

import io.logbee.keyscore.model.configuration.{Configuration, ParameterSet, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class DifferentialQuotientLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A DifferentialQuotient" - {

    val configuration = Configuration(parameterSet = ParameterSet(Seq(
      TextParameter(DifferentialQuotientLogic.xFieldNameParameter.ref, "time"),
      TextParameter(DifferentialQuotientLogic.yFieldNameParameter.ref, "voltage"),
      TextParameter(DifferentialQuotientLogic.targetFieldNameParameter.ref, "slope")
    )))

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

    "should let pass datasets unchanged if they do not contain all required fields" in new TestStreamForFilter[DifferentialQuotientLogic](configuration) {

      sink.request(1)
      source.sendNext(unwantedSample)

      sink.requestNext() shouldBe unwantedSample
    }

    "should set the computed differential quotient to 0 on the first dataset" in new TestStreamForFilter[DifferentialQuotientLogic](configuration) {

      sink.request(1)
      source.sendNext(sample1)

      val dataset = sink.requestNext()
      val slope = findSlopeField(dataset.records.head)

      slope shouldBe 'defined
      slope.get.toDecimalField.value shouldBe 0
    }

    "should compute the differential quotient of the specified field between two consecutive datasets" in new TestStreamForFilter[DifferentialQuotientLogic](configuration) {

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

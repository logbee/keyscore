package io.logbee.keyscore.pipeline.contrib.math

import io.logbee.keyscore.model.configuration.{Configuration, FieldNameListParameter, ParameterSet}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.contrib.math.AddFields.fieldListParameter
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class AddFieldsSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  val sample = Dataset(records = Record(
    Field("field1", TextValue("123")),
    Field("field2", DecimalValue(123.0)),
    Field("field3", NumberValue(123)),
  ))

  "A AddFieldsFilter" - {

    val configuration1 = Configuration(parameterSet = ParameterSet(Seq(
      FieldNameListParameter(fieldListParameter.ref, Seq(
        "field1", "field2", "field3"
      )))))

    val configuration2 = Configuration(parameterSet = ParameterSet(Seq(
      FieldNameListParameter(fieldListParameter.ref, Seq("field1"))
    )))

    "should add sum of given numeric fields" in new TestStreamForFilter[AddFields](configuration1) {

      whenReady(filterFuture) { filter =>

        source.sendNext(sample)

        sink.requestNext() shouldBe sample.update(_.records := List(sample.records.head.update(_.fields :+= Field("result", DecimalValue(246.0)))))

        whenReady(filter.configure(configuration2)) { _ =>

          source.sendNext(sample)

          sink.requestNext() shouldBe sample.update(_.records := List(sample.records.head.update(_.fields :+= Field("result", DecimalValue(0.0)))))
        }
      }
    }
  }
}

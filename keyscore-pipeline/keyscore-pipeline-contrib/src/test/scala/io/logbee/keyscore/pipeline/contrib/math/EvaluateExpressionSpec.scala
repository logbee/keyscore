package io.logbee.keyscore.pipeline.contrib.math

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.contrib.math.EvaluateExpression.expressionFieldParameter
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class EvaluateExpressionSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  val sample = Dataset(records = Record(
    Field("a", DecimalValue(3)),
    Field("b", DecimalValue(7.0)),
    Field("c", NumberValue(2)),
  ))

  "A AddFieldsFilter" - {

    val configuration1 = Configuration(parameterSet = ParameterSet(Seq(
      TextParameter(expressionFieldParameter.ref,
        "a + b + c"
      ))))

    val configuration2 = Configuration(parameterSet = ParameterSet(Seq(
      TextParameter(expressionFieldParameter.ref,
        "a * b * c"
      ))))

    val invalidConfiguration1 = Configuration(parameterSet = ParameterSet(Seq(
      TextParameter(expressionFieldParameter.ref,
        "a * b # c"
      ))))

    "should evaluate given expression and write result" in new TestStreamForFilter[EvaluateExpression](configuration1) {

      whenReady(filterFuture) { filter =>

        source.sendNext(sample)

        sink.requestNext() shouldBe sample.update(_.records := List(sample.records.head.update(_.fields :+= Field("result", DecimalValue(12.0)))))

        whenReady(filter.configure(configuration2)) { _ =>

          source.sendNext(sample)

          sink.requestNext() shouldBe sample.update(_.records := List(sample.records.head.update(_.fields :+= Field("result", DecimalValue(42.0)))))
        }

        whenReady(filter.configure(invalidConfiguration1)) { _ =>

          source.sendNext(sample)

          sink.requestNext() shouldBe sample
        }
      }
    }
  }
}

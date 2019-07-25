package io.logbee.keyscore.pipeline.contrib.math

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.contrib.math.CalcLogic.expressionParameter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class CalcLogicSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  case class Fixture(expression: String, sample: Dataset, expectation: Option[Double])

  "A CalcLogic" - {

    val sample1 = Dataset(records = Record(
      Field("a", DecimalValue(3)),
      Field("b", DecimalValue(7.0)),
      Field("c", NumberValue(2)),
    ))

    val sample2_dot_underscore = Dataset(records = Record(
      Field("a.field", DecimalValue(3)),
      Field("b_field", DecimalValue(7.0)),
      Field("c", NumberValue(2)),
    ))

    val sample3_whitespace = Dataset(records = Record(
      Field("a field", DecimalValue(3)),
      Field("b field", DecimalValue(7.0)),
      Field("c", NumberValue(2)),
    ))

    Seq(
      Fixture(
        expression = "a + b + c",
        sample = sample1,
        expectation = 12.0),
      Fixture(
        expression = "a * b * c",
        sample = sample1,
        expectation = 42.0),
      Fixture(
        expression = "a / c",
        sample = sample1,
        expectation = 1.5),
      Fixture(
        expression = "b - c",
        sample = sample1,
        expectation = 5.0),
      Fixture(
        expression = "a.field + b_field",
        sample = sample2_dot_underscore,
        expectation = 10.0),
      Fixture(
        expression = "a_field + b_field",
        sample = sample3_whitespace,
        expectation = 10.0),
      Fixture(
        expression = "b_field + c field",
        sample = sample3_whitespace,
        expectation = None),
    )

    .foreach { case Fixture(expression, sample, expectation) =>

      s"when configured with '$expression'" - {

        val configuration = Configuration(parameterSet = ParameterSet(Seq(
          TextParameter(expressionParameter.ref, expression))))

        s"should compute '$expectation'" in new TestStreamForFilter[CalcLogic](configuration) {

          whenReady(filterFuture) { _ =>

            source.sendNext(sample)

            if (expectation.nonEmpty) {
              sink.requestNext() shouldBe sample.update(_.records := List(sample.records.head.update(_.fields :+= Field("result", DecimalValue(expectation.get)))))
            }
            else {
              sink.requestNext() shouldBe sample
            }
          }
        }
      }
    }
  }
}

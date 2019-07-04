package io.logbee.keyscore.pipeline.contrib.math

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.contrib.math.CalcLogic.expressionParameter
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class CalcLogicSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  case class Fixture(expression: String, sample: Dataset, expectation: Double)

  "A CalcLogic" - {

    val sample = Dataset(records = Record(
      Field("a", DecimalValue(3)),
      Field("b", DecimalValue(7.0)),
      Field("c", NumberValue(2)),
    ))

    Seq(
      Fixture(
        expression = "a + b + c",
        sample = sample,
        expectation = 12.0),
      Fixture(
        expression = "a * b * c",
        sample = sample,
        expectation = 42.0),
      Fixture(
        expression = "a / c",
        sample = sample,
        expectation = 1.5),
      Fixture(
        expression = "b - c",
        sample = sample,
        expectation = 5.0),
    )

    .foreach { case Fixture(expression, sample, expectation) =>

      s"when configured with '$expression'" - {

        val configuration = Configuration(parameterSet = ParameterSet(Seq(
          TextParameter(expressionParameter.ref, expression))))

        s"should compute '$expectation'" in new TestStreamForFilter[CalcLogic](configuration) {

          whenReady(filterFuture) { _ =>

            source.sendNext(sample)

            sink.requestNext() shouldBe sample.update(_.records := List(sample.records.head.update(_.fields :+= Field("result", DecimalValue(expectation)))))
          }
        }
      }
    }
  }
}

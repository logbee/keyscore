package io.logbee.keyscore.pipeline.contrib.decoder.json

import io.logbee.keyscore.model.configuration.{BooleanParameter, Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.model.util.Using
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonPathJsonExtractorLogicSpec extends AnyFreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A JsonPathJsonExtractorLogic" - {

    val plainJson = Using.using(getClass.getResourceAsStream("/JsonPathJsonExtractorLogic.example.json")) { stream =>
      scala.io.Source.fromInputStream(stream).mkString
    }

    Seq(
      Fixture(
        title = "should extract the three temperatures as records beside the raw-data",
        expression = "$.device.temperatures.data",
        sample = plainJson,
        configuration = Configuration(
          TextParameter(JsonPathJsonExtractorLogic.sourceFieldNameParameter, "sample"),
          TextParameter(JsonPathJsonExtractorLogic.jsonpathParameter, "$.device.temperatures.data")
        ),
        expectation = Dataset(
          Record(
            Field("sample", TextValue(plainJson))
          ),
          Record(
            Field("time", NumberValue(1)),
            Field("temperature", NumberValue(5)),
          ),
          Record(
            Field("time", NumberValue(3)),
            Field("temperature", NumberValue(7)),
          ),
          Record(
            Field("time", NumberValue(7)),
            Field("temperature", NumberValue(15))
          )
        )
      ),
      Fixture(
        title = "should extract two records beside the raw-data",
        expression = "$.device.hardware.sensors.*",
        sample = plainJson,
        configuration = Configuration(
          TextParameter(JsonPathJsonExtractorLogic.sourceFieldNameParameter, "sample"),
          TextParameter(JsonPathJsonExtractorLogic.jsonpathParameter, "$.device.hardware.sensors.*")
        ),
        expectation = Dataset(
          Record(
            Field("sample", TextValue(plainJson))
          ),
          Record(
            Field("name", TextValue("a1")),
            Field("id", NumberValue(3)),
          ),
          Record(
            Field("name", TextValue("a2")),
            Field("id", NumberValue(35)),
          )
        )
      )
    ).foreach {
      case Fixture(title, expression, configuration, sample, expectation) =>
        s"when configured with '$expression'" - {

          s"$title" in new TestStreamForFilter[JsonPathJsonExtractorLogic](configuration) {

            whenReady(filterFuture) { _ =>

              val sampleDataset = Dataset(Record(
                Field("sample", TextValue(sample))
              ))

              sink.request(1)
              source.sendNext(sampleDataset)

              val result = sink.requestNext()

              result shouldBe expectation
            }
          }
        }
    }

    "when configured to remove the source field" - {

      val configuration = Configuration(
        TextParameter(JsonPathJsonExtractorLogic.sourceFieldNameParameter, "rawJson"),
        TextParameter(JsonPathJsonExtractorLogic.jsonpathParameter, "$.device.name"),
        BooleanParameter(JsonPathJsonExtractorLogic.removeSourceFieldParameter, true),
      )

      "should remove the source field" in new TestStreamForFilter[JsonPathJsonExtractorLogic](configuration) {

        whenReady(filterFuture) { _ =>

          val sample = Dataset(Record(
            Field("rawJson", TextValue(plainJson))
          ))

          sink.request(1)
          source.sendNext(sample)

          val dataset = sink.requestNext()
          dataset.records should contain only (
            Record(
              Field("name", TextValue("robot"))
            )
          )
        }
      }
    }
  }

  case class Fixture(title: String, expression: String, configuration: Configuration, sample: String, expectation: Dataset)
}

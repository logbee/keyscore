package io.logbee.keyscore.pipeline.contrib.decoder.json

import io.logbee.keyscore.model.configuration.{BooleanParameter, Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.model.util.Using
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonPathJsonExtractorLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A JsonPathJsonExtractorLogic" - {

    val plainJson = Using.using(getClass.getResourceAsStream("/JsonPathJsonExtractorLogic.example.json")) { stream =>
      scala.io.Source.fromInputStream(stream).mkString
    }

    "when configured to extract 'device.temperatures.data' from rawJson" - {

      val configuration = Configuration(
        TextParameter(JsonPathJsonExtractorLogic.sourceFieldNameParameter, "rawJson"),
        TextParameter(JsonPathJsonExtractorLogic.jsonpathParameter, "$.device.temperatures.data")
      )

      "should extract the data as recods" in new TestStreamForFilter[JsonPathJsonExtractorLogic](configuration) {

        whenReady(filterFuture) { _ =>

          val sample = Dataset(Record(
            Field("rawJson", TextValue(plainJson))
          ))

          sink.request(1)
          source.sendNext(sample)

          val dataset = sink.requestNext()
          dataset.records should contain only (
            Record(
              Field("rawJson", TextValue(plainJson))
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
}

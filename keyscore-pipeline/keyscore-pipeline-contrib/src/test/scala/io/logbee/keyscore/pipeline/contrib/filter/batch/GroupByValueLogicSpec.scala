package io.logbee.keyscore.pipeline.contrib.filter.batch

import io.logbee.keyscore.model.configuration.{Configuration, FieldNameParameter}
import io.logbee.keyscore.model.data.{Record, _}
import io.logbee.keyscore.pipeline.contrib.test.TestStreamWithSourceAndSink
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._


@RunWith(classOf[JUnitRunner])
class GroupByValueLogicSpec extends FreeSpec with ScalaFutures with Matchers with TestSystemWithMaterializerAndExecutionContext {

  "A GroupByValueLogic" - {

    "with inactive time window" - {

      val configuration = Configuration(parameters = Seq(
        FieldNameParameter(GroupByValueLogic.fieldNameParameter.ref, "key")
      ))

      val samplesA = Seq(
        Dataset(Record(
          Field("key", TextValue("weather-forecast")),
          Field("message", TextValue("Its a cloudy day!"))
        )),
        Dataset(
          Record(
            Field("key", TextValue("weather-forecast")),
            Field("temperature", DecimalValue(23.5))
          ),
          Record(
            Field("sensor", TextValue("XC90")),
          )
        )
      )

      val otherSample = Dataset(Record(
        Field("key", TextValue("daily-news")),
        Field("message", TextValue("No news today."))
      ))

      "should let records pass which does not contain the configured field" in new TestStreamWithSourceAndSink(configuration, classOf[GroupByValueLogic]) {

        val samples = Seq(
          Dataset(Record(
            Field("message", TextValue("This is an unknown message."))
          )),
          Dataset(Record(
            Field("foo", TextValue("bar"))
          ))
        )

        whenReady(filterFuture) { _ =>
          samples.foreach(source.sendNext)
          sink.request(3)
          sink.requestNext(samples.head)
          sink.requestNext(samples.last)
          sink.expectNoMessage(2 seconds)
        }
      }

      "should not let records pass when the value of the configured field does not change" in new TestStreamWithSourceAndSink(configuration, classOf[GroupByValueLogic]) {

        whenReady(filterFuture) { filter =>
          samplesA.foreach(source.sendNext)
          sink.request(1)
          sink.expectNoMessage(2 seconds)
        }
      }

      "should combine consecutive datasets by the value of the configured field" in new TestStreamWithSourceAndSink(configuration, classOf[GroupByValueLogic]) {

        whenReady(filterFuture) { filter =>

          sink.request(3)

          samplesA.foreach(source.sendNext)

          sink.expectNoMessage(1 seconds)

          source.sendNext(otherSample)

          var actual = sink.requestNext()

          actual.records should contain only(
            Record(
              Field("key", TextValue("weather-forecast")),
              Field("message", TextValue("Its a cloudy day!")),
            ),
            Record(
              Field("key", TextValue("weather-forecast")),
              Field("temperature", DecimalValue(23.5))
            ),
            Record(
              Field("sensor", TextValue("XC90")),
            )
          )

          sink.expectNoMessage(1 seconds)

          samplesA.foreach(source.sendNext)

          actual = sink.requestNext()

          actual shouldBe otherSample

          sink.expectNoMessage(1 seconds)
        }
      }
    }
  }
}

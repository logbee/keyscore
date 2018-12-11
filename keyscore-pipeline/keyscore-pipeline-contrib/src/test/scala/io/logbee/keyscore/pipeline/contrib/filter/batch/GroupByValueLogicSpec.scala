package io.logbee.keyscore.pipeline.contrib.filter.batch

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data.{Record, _}
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.contrib.test.TestStreamFor
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

      val configuration = Configuration(parameterSet = ParameterSet(Seq(
        FieldNameParameter(GroupByValueLogic.fieldNameParameter, "key")
      )))

      val samples = Seq(
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

      "should let records pass which does not contain the configured field" in new TestStreamFor[GroupByValueLogic](configuration) {

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

      "should not let records pass when the value of the configured field does not change" in new TestStreamFor[GroupByValueLogic](configuration) {

        whenReady(filterFuture) { _ =>
          samples.foreach(source.sendNext)
          sink.request(1)
          sink.expectNoMessage(2 seconds)
        }
      }

      "should group consecutive datasets by the value of the configured field" in new TestStreamFor[GroupByValueLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(3)

          samples.foreach(source.sendNext)

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

          samples.foreach(source.sendNext)

          actual = sink.requestNext()

          actual shouldBe otherSample

          sink.expectNoMessage(1 seconds)
        }
      }
    }

    "with active time window" - {

      val configuration = Configuration(parameterSet = ParameterSet(Seq(
        FieldNameParameter(GroupByValueLogic.fieldNameParameter, "key"),
        ChoiceParameter(GroupByValueLogic.windowParameter, GroupByValueLogic.timeWindowChoice.name),
        NumberParameter(GroupByValueLogic.timeWindowMillisParameter, 1000),
      )))

      val samples = Seq(
        Dataset(
          Record(
            Field("key", TextValue("weather-forecast")),
            Field("message", TextValue("Its a cloudy day."))
          )
        )
      )

      "should push out a single dataset when time window has expired" in new TestStreamFor[GroupByValueLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(samples.head)

          sink.expectNoMessage(remaining = 1000 millis)

          val actual = sink.requestNext(500 millis)

          actual shouldBe samples.head
        }
      }
    }
  }
}

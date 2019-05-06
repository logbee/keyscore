package io.logbee.keyscore.pipeline.contrib.filter.batch

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.contrib.filter.batch.AbstractGroupingLogic._
import io.logbee.keyscore.pipeline.contrib.test.TestStreamFor
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * This Test also tests the metrics of the [[AbstractGroupingLogic]].
  */
@RunWith(classOf[JUnitRunner])
class GroupByValueLogicSpec extends FreeSpec with ScalaFutures with Matchers with TestSystemWithMaterializerAndExecutionContext {

  "A GroupByValueLogic" - {

    val sampleDatasets = Seq(
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

    val singleDataset = Dataset(Record(
      Field("key", TextValue("daily-news")),
      Field("message", TextValue("No news today."))
    ))

    val expectedGroup = Seq(
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

    "with inactive time window" - {

      val configuration = Configuration(
        FieldNameParameter(GroupByValueLogic.fieldNameParameter, "key")
      )

      "should let records pass which do not contain the configured field" in new TestStreamFor[GroupByValueLogic](configuration) {

        private val datasets = Seq(
          Dataset(Record(
            Field("message", TextValue("This is an unknown message."))
          )),
          Dataset(Record(
            Field("foo", TextValue("bar"))
          ))
        )

        whenReady(filterFuture) { filter =>
          datasets.foreach(source.sendNext)
          sink.request(3)
          sink.requestNext(datasets.head)
          sink.requestNext(datasets.last)
          sink.expectNoMessage(remaining = 2 seconds)

          whenReady(filter.scrape()) { mc =>
            mc.find(queuedEntries).get.value shouldBe 2
            mc.find(pushedEntries).get.value shouldBe 2
//            mc.find(queueMemory).get.value shouldBe > (0L)
          }
        }
      }

      "should not let records pass when the value of the configured field does not change" in new TestStreamFor[GroupByValueLogic](configuration) {

        whenReady(filterFuture) { _ =>
          sampleDatasets.foreach(source.sendNext)
          sink.request(1)
          sink.expectNoMessage(remaining = 2 seconds)
        }
      }

      "should group consecutive datasets by the value of the configured field" in new TestStreamFor[GroupByValueLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(3)

          sampleDatasets.foreach(source.sendNext)

          sink.expectNoMessage(remaining = 1 seconds)

          source.sendNext(singleDataset)

          var actual = sink.requestNext()

          actual.records should contain only (expectedGroup: _*)

          sink.expectNoMessage(remaining = 1 seconds)

          sampleDatasets.foreach(source.sendNext)

          actual = sink.requestNext()

          actual shouldBe singleDataset

          sink.expectNoMessage(remaining = 1 seconds)
        }
      }
    }

    "with active time window" - {

      val configuration = Configuration(
        FieldNameParameter(GroupByValueLogic.fieldNameParameter, "key"),
        BooleanParameter(GroupByValueLogic.timeWindowActiveParameter, value = true),
        NumberParameter(GroupByValueLogic.timeWindowMillisParameter, 1000),
      )

      "should push out a single dataset when time window has expired" in new TestStreamFor[GroupByValueLogic](configuration) {

        private val sample = Dataset(
          Record(
            Field("key", TextValue("weather-forecast")),
            Field("message", TextValue("Its a cloudy day."))
          )
        )

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          sink.expectNoMessage(remaining = 1000 millis)

          val actual = sink.requestNext(1000 millis)

          actual shouldBe sample
        }
      }

      "should group consecutive datasets but not push until the time window has expired" in new TestStreamFor[GroupByValueLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(3)

          sampleDatasets.foreach(source.sendNext)
          source.sendNext(singleDataset)

          sink.expectNoMessage(remaining = 1000 millis)

          var actual = sink.requestNext(1000 millis)

          actual.records should contain only (expectedGroup:_*)

          actual = sink.requestNext(1000 millis)

          actual shouldBe singleDataset
        }
      }
    }
  }
}

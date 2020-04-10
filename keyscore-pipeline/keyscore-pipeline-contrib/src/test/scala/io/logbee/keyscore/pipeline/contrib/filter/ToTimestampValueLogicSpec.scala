package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{ChoiceParameter, Configuration, FieldNameParameter, ParameterSet, TextParameter}
import io.logbee.keyscore.model.data.{Dataset, DecimalValue, Field, NumberValue, Record, TextValue, TimestampValue}
import io.logbee.keyscore.pipeline.testkit.{TestActorSystem, TestStreamForFilter}
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.filter.ToTimestampValueLogic.{formatParameter, sourceFieldNameParameter, sourceTimeZoneParameter}
import org.scalatest.Inside
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

@RunWith(classOf[JUnitRunner])
class ToTimestampValueLogicSpec extends AnyFreeSpec with Matchers with Inside with ScalaFutures with TestActorSystem {

  val configuration1 = Configuration(parameterSet = ParameterSet(Seq(
    FieldNameParameter(sourceFieldNameParameter.ref, "text-timestamp"),
    TextParameter(formatParameter.ref, "yyyy.MM.dd HH:mm:ss.SSS"),
  )))

  val configuration2 = Configuration(parameterSet = ParameterSet(Seq(
    FieldNameParameter(sourceFieldNameParameter.ref, "text-timestamp"),
    TextParameter(formatParameter.ref, "yyyy.MM.dd HH:mm:ss.SSS"),
    ChoiceParameter(sourceTimeZoneParameter.ref, "GMT+1"),
  )))

  val sample = Dataset(Record(
      Field("text-timestamp", TextValue("2019.08.21 14:32:53.123")),
      Field("seconds-timestamp", NumberValue(1570005875)),   // 2019-10-02T08:44:35.000+00:00
      Field("millis-timestamp", NumberValue(1570005875042L)) // 2019-10-02T08:44:35.042+00:00
  ))

  val nonMatchingDataset = Dataset(
    Record(
      Field("foo", DecimalValue(42.0)),
      Field("bar", TextValue("Hello World!"))
    ),
    Record(
      Field("foo", DecimalValue(5.3)),
      Field("bar", TextValue("Bye Bye!"))
    )
  )

  "A ToTimestampValueLogic" - {

    "should passthrough datasets which do not have the specified source field" in new TestStreamForFilter[ToTimestampValueLogic](configuration1) {

      whenReady(filterFuture) { _ =>

        sink.request(1)
        source.sendNext(nonMatchingDataset)

        sink.requestNext().records shouldBe nonMatchingDataset.records
      }
    }

    "should convert text to a timestamp" in new TestStreamForFilter[ToTimestampValueLogic](configuration1) {

      whenReady(filterFuture) { _ =>

        sink.request(1)
        source.sendNext(sample)

        sink.requestNext() shouldBe Dataset(Record(
          Field("text-timestamp", TimestampValue(1566397973, 123000000)),
          Field("seconds-timestamp", NumberValue(1570005875)),
          Field("millis-timestamp", NumberValue(1570005875042L))
        ))
      }
    }

    "should convert text with the given time-zone to timestamp" in new TestStreamForFilter[ToTimestampValueLogic](configuration2) {

      whenReady(filterFuture) { _ =>

        sink.request(1)
        source.sendNext(sample)

        sink.requestNext() shouldBe Dataset(Record(
          Field("text-timestamp", TimestampValue(1566394373, 123000000)),
          Field("seconds-timestamp", NumberValue(1570005875)),
          Field("millis-timestamp", NumberValue(1570005875042L))
        ))
      }
    }

    val configurationForSeconds = Configuration(parameterSet = ParameterSet(Seq(
      FieldNameParameter(sourceFieldNameParameter.ref, "seconds-timestamp"),
      ChoiceParameter(ToTimestampValueLogic.sourceFieldTypeParameter.ref, ToTimestampValueLogic.NumberSeconds),
    )))

    "should convert seconds to a timestamp" in new TestStreamForFilter[ToTimestampValueLogic](configurationForSeconds) {

      whenReady(filterFuture) { _ =>

        sink.request(1)
        source.sendNext(sample)

        sink.requestNext() shouldBe Dataset(Record(
          Field("text-timestamp", TextValue("2019.08.21 14:32:53.123")),
          Field("seconds-timestamp", TimestampValue(1570005875)),
          Field("millis-timestamp", NumberValue(1570005875042L))
        ))
      }
    }

    val configurationForMillis = Configuration(parameterSet = ParameterSet(Seq(
      FieldNameParameter(sourceFieldNameParameter.ref, "millis-timestamp"),
      ChoiceParameter(ToTimestampValueLogic.sourceFieldTypeParameter.ref, ToTimestampValueLogic.NumberMillis),
    )))

    "should convert millis to a timestamp" in new TestStreamForFilter[ToTimestampValueLogic](configurationForMillis) {

      whenReady(filterFuture) { _ =>

        sink.request(1)
        source.sendNext(sample)

        sink.requestNext() shouldBe Dataset(Record(
          Field("text-timestamp", TextValue("2019.08.21 14:32:53.123")),
          Field("seconds-timestamp", NumberValue(1570005875)),
          Field("millis-timestamp", TimestampValue(1570005875, 42000000))
        ))
      }
    }
  }
}

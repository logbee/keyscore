package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import java.time.ZoneId

import io.logbee.keyscore.model.data.{Field, TextValue, TimestampValue}
import org.junit.runner.RunWith
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class TextToTimestampDirectiveSpec extends AnyFreeSpec with Matchers {

  val samples = Seq(
    ("yyyy-MM-dd HH:mm:ss", "2019-02-15 17:52:14", None, TimestampValue(1550253134)),
    ("yyyy-MM-dd HH:mm:ss.SSS", "2019-02-15 17:52:14.100", None, TimestampValue(1550253134, 100000000)),
    ("yyyy-MM-dd'T'HH:mm:ss", "2019-02-15T17:52:14", None, TimestampValue(1550253134)),
    ("yyyy-MM-dd'T'HH:mm:ss", "2019-02-15T17:52:14", Some(ZoneId.of("UTC")), TimestampValue(1550253134)),
    ("yyyy-MM-dd'T'HH:mm:ss", "2019-05-12T09:30:00", Some(ZoneId.of("Australia/Darwin")), TimestampValue(1557619200)),
    ("yyyy-MM-dd'T'HH:mm:ss", "2019-03-31T02:30:00", Some(ZoneId.of("Europe/Paris")), TimestampValue(1553995800)), // Daylight Saving: adjust forward
    ("yyyy-MM-dd'T'HH:mm:ss", "2019-10-27T02:30:00", Some(ZoneId.of("Europe/Paris")), TimestampValue(1572136200)), // Daylight Saving: adjust backward
  )

  "A ToTimestampDirective" - {

    samples.foreach { case (pattern, sample, sourceTimeZone, expectedValue) =>

      s"should convert $sample to $expectedValue given the pattern '$pattern', from time zone '$sourceTimeZone'" in {

        val directive = TextToTimestampDirective(pattern, sourceTimeZone)
        val field = Field("timestamp", TextValue(sample))

        directive.invoke(field) shouldBe Seq(Field("timestamp", expectedValue))
      }
    }
  }
}

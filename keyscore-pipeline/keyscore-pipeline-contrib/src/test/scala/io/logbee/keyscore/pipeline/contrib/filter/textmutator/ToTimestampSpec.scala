package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import io.logbee.keyscore.model.data.{Field, TextValue, TimestampValue}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ToTimestampSpec  extends FreeSpec with Matchers {

  val samples = Seq(
    ("yyyy-MM-dd HH:mm:ss", "2019-02-15 17:52:14", TimestampValue(1550253134)),
    ("yyyy-MM-dd HH:mm:ss.SSS", "2019-02-15 17:52:14.100", TimestampValue(1550253134, 100000000)),
    ("yyyy-MM-dd'T'HH:mm:ss", "2019-02-15T17:52:14", TimestampValue(1550253134))
  )

  "A ToTimestampDirective" - {

    samples.foreach { case (pattern, sample, expectedValue) =>

      s"should convert $sample to $expectedValue given $pattern" in {

        val directive = ToTimestampDirective(pattern)
        val field = Field("timestamp", TextValue(sample))

        directive.invoke(field) shouldBe Field("timestamp", expectedValue)
      }
    }
  }
}

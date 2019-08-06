package io.logbee.keyscore.commons.ehcache

import java.time.Duration.ofSeconds
import java.util.UUID

import com.google.protobuf.timestamp.Timestamp
import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.commons.ehcache.MetricsCache.Configuration
import io.logbee.keyscore.model.data.TimestampValue
import io.logbee.keyscore.model.metrics.{CounterMetric, NumberGaugeMetric, MetricsCollection}
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MetricsCacheSpec extends FreeSpec with Matchers {

  val id01: UUID = UUID.fromString("edf7758a-9c90-4a18-94bd-87acf5ca6ecb")
  val id02: UUID = UUID.fromString("40407701-e79b-43f0-aad3-65709c3578fc")
  val id03: UUID = UUID.fromString("2b90e0f6-922a-42eb-a36e-dc1311aafd10")
  val id07: UUID = UUID.fromString("4d509139-9c3a-463d-aa4e-022d1c3809df")
  val id08: UUID = UUID.fromString("115657dc-4c16-4e78-9b85-85ceb620ad67")
  val id09: UUID = UUID.fromString("af52edd8-9ebc-4c5e-90cb-817bf7ccd53f")
  val id10: UUID = UUID.fromString("315b6e7f-90ef-4f75-a6ef-aa6271f182d5")
  val id11: UUID = UUID.fromString("7dadd770-e7d3-4d42-b860-fa61ca24382b")
  val id12: UUID = UUID.fromString("6281dbd5-a8b2-4e89-bfbf-c60bcc36c068")
  val id13: UUID = UUID.fromString("eb800877-2f90-49dc-97aa-f0c22e2b003e")
    
  val m01 = MetricsCollection(List(CounterMetric("cm1", value = 11, timestamp = Some(now))))
  val m02 = MetricsCollection(List(CounterMetric("cm2", value = 12, timestamp = Some(now))))

  val m03 = MetricsCollection(List(CounterMetric("cm3", value = 13, timestamp = Some(TimestampValue(9,25)))))
  val m04 = MetricsCollection(List(CounterMetric("cm4", value = 14, timestamp = Some(TimestampValue(11,27)))))
  val m05 = MetricsCollection(List(CounterMetric("cm5", value = 15, timestamp = Some(TimestampValue(11,42)))))
  val m06 = MetricsCollection(List(CounterMetric("cm6", value = 16, timestamp = Some(TimestampValue(12,73)))))

  val m07 = MetricsCollection(List(CounterMetric("cm7", value = 17, timestamp = Some(now))))
  val m08 = MetricsCollection(List(CounterMetric("cm8", value = 18, timestamp = Some(now))))
  val m09 = MetricsCollection(List(CounterMetric("cm9", value = 19, timestamp = Some(now))))
  val m10 = MetricsCollection(List(CounterMetric("cm0", value = 10, timestamp = Some(now))))
  val m11 = MetricsCollection(List(NumberGaugeMetric("gm1", value = 21, timestamp = Some(now))))
  val m12 = MetricsCollection(List(NumberGaugeMetric("gm2", value = 22, timestamp = Some(now))))
  val m13 = MetricsCollection(List(NumberGaugeMetric("gm3", value = 23, timestamp = Some(now))))

  "A Metrics Cache" - {

    "should put a multiple metrics the cache" in {
      val metricsCache = new MetricsCache(Configuration(10L, 10000000L, ofSeconds(5)))

      metricsCache.put(id01, m01)
      metricsCache.put(id02, m02)
      metricsCache.put(id07, m07)
      metricsCache.put(id08, m08)
      metricsCache.put(id09, m09)
      metricsCache.put(id10, m10)
      metricsCache.put(id13, m13)

      Thread.sleep(1000)

      metricsCache.getOldest(id01).get shouldBe m01
      metricsCache.getOldest(id07).get shouldBe m07
      metricsCache.getNewest(id07).get shouldBe m07
      metricsCache.getOldest(id13).get shouldBe m13

      metricsCache.clear()
      metricsCache.getOldest(id03) shouldBe None
      metricsCache.getAll(id03, Timestamp(0, 0),Timestamp(42, 42), 10) shouldBe Seq()
    }

    "should update the tuple for multiple entries for a single uuid" in {
      val metricsCache = new MetricsCache(Configuration(10L, 10000000L, ofSeconds(5)))

      metricsCache.put(id03, m03)
      metricsCache.put(id03, m04)
      metricsCache.put(id03, m05)
      metricsCache.put(id03, m06)

      Thread.sleep(1000)

      metricsCache.getAll(id03, earliest = Timestamp(8, 25), latest = Timestamp(13, 73), limit = 10).size shouldBe 4
      metricsCache.getAll(id03, earliest = Timestamp(8, 25), latest = Timestamp(13, 73), limit = 3).size shouldBe 3
      metricsCache.getAll(id03, earliest = Timestamp(9, 26), latest = Timestamp(11, 72), limit = 3).size shouldBe 2
      metricsCache.getAll(id03, earliest = Timestamp(9, 25), latest = Timestamp(12, 73), limit = 10).size shouldBe 4
      metricsCache.getAll(id03, earliest = Timestamp(9, 25), latest = Timestamp(12, 72), limit = 10).size shouldBe 3
      metricsCache.getAll(id03, earliest = Timestamp(9, 25), latest = Timestamp(12, 74), limit = 10).size shouldBe 4
      metricsCache.getAll(id03, earliest = Timestamp(11, 26), latest = Timestamp(12, 74), limit = 10).size shouldBe 3
      metricsCache.getAll(id03, earliest = Timestamp(10, 27), latest = Timestamp(13, 73), limit = 10).size shouldBe 3

      metricsCache.getOldest(id03).get shouldBe m03
      metricsCache.getNewest(id03).get shouldBe m06

    }

    "should remove all old entries" in {
      val metricsCache = new MetricsCache(Configuration(10L, 10000000L, ofSeconds(1)))

      metricsCache.put(id11, m11)

      Thread.sleep(1000)
      metricsCache.put(id12, m12)

      metricsCache.getOldest(id12) shouldNot be (None)
      metricsCache.getOldest(id11) shouldBe None
      
      metricsCache.close()

    }

  }

  private def now: TimestampValue = {
    val now = Timestamps.fromMillis(System.currentTimeMillis())
    TimestampValue(now.getSeconds, now.getNanos)
  }


}

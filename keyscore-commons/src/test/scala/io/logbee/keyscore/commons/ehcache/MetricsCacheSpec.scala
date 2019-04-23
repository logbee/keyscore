package io.logbee.keyscore.commons.ehcache

import java.time.Duration.ofSeconds
import java.util.UUID

import io.logbee.keyscore.model.metrics.{CounterMetric, GaugeMetric, MetricsCollection}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}


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

  val m01 = MetricsCollection(Seq(CounterMetric(name = "cm1", value = 1.1)))
  val m02 = MetricsCollection(Seq(CounterMetric(name = "cm2", value = 1.2)))
  val m03 = MetricsCollection(Seq(CounterMetric(name = "cm3", value = 1.3)))
  val m04 = MetricsCollection(Seq(CounterMetric(name = "cm4", value = 1.4)))
  val m05 = MetricsCollection(Seq(CounterMetric(name = "cm5", value = 1.5)))
  val m06 = MetricsCollection(Seq(CounterMetric(name = "cm6", value = 1.6)))
  val m07 = MetricsCollection(Seq(CounterMetric(name = "cm7", value = 1.7)))
  val m08 = MetricsCollection(Seq(CounterMetric(name = "cm8", value = 1.8)))
  val m09 = MetricsCollection(Seq(CounterMetric(name = "cm9", value = 1.9)))
  val m10 = MetricsCollection(Seq(CounterMetric(name = "cm0", value = 1.0)))
  val m11 = MetricsCollection(Seq(GaugeMetric(name = "gm1", value = 2.1)))
  val m12 = MetricsCollection(Seq(GaugeMetric(name = "gm2", value = 2.2)))
  val m13 = MetricsCollection(Seq(GaugeMetric(name = "gm3", value = 2.3)))

  "A Metrics CacheManager" - {

    "should put a multiple metrics the cache" in {
      val metricsCacheManager = new MetricsCache(10L, 10L, ofSeconds(5))

      metricsCacheManager.put(id01, m01)
      metricsCacheManager.put(id02, m02)
      metricsCacheManager.put(id07, m07)
      metricsCacheManager.put(id08, m08)
      metricsCacheManager.put(id09, m09)
      metricsCacheManager.put(id10, m10)
      metricsCacheManager.put(id13, m13)

      Thread.sleep(1000)

      metricsCacheManager.getOldest(id01).get shouldBe m01
      metricsCacheManager.getOldest(id07).get shouldBe m07
      metricsCacheManager.getNewest(id07).get shouldBe m07
      metricsCacheManager.getOldest(id13).get shouldBe m13

      metricsCacheManager.clear()
      metricsCacheManager.getOldest(id03) shouldBe None
    }

    "should update the tuple for multiple entries for a single uuid" in {
      val metricsCacheManager = new MetricsCache(10L, 10L, ofSeconds(5))

      metricsCacheManager.put(id03, m03)
      metricsCacheManager.put(id03, m04)
      metricsCacheManager.put(id03, m05)
      metricsCacheManager.put(id03, m06)

      Thread.sleep(1000)

      val seq = metricsCacheManager.getAll(id03)
      seq.size shouldBe 4

      metricsCacheManager.getOldest(id03).get shouldBe m03
      metricsCacheManager.getNewest(id03).get shouldBe m06

    }

    "should remove all old entries" in {
      val metricsCacheManager = new MetricsCache(10L, 10L, ofSeconds(1))

      metricsCacheManager.put(id11, m11)

      Thread.sleep(1000)
      metricsCacheManager.put(id12, m12)

      metricsCacheManager.getOldest(id12) shouldNot be (None)
      metricsCacheManager.getOldest(id11) shouldBe None

    }

  }


}

package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.metrics._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class BaseMetricsCollectionSpec extends FreeSpec with Matchers {

  "A MetricsCollection" - {

    val uuid = UUID.fromString("69de6234-e053-4d33-8761-49d3228d8287")

    val otherName = s"$uuid._"
    val otherDesc = CounterMetricDescriptor("_")
    val other = CounterMetric(otherName, timestamp = None, 0.1)
    val falseDesc = CounterMetricDescriptor("__")

    val counterName = s"$uuid._counter"
    val counterMetricDesc = CounterMetricDescriptor("_counter")
    val counterMetric = CounterMetric(counterName, timestamp = None, 1.0)

    val gaugeName = s"$uuid._gauge"
    val gaugeMetricDesc = GaugeMetricDescriptor("_gauge")
    val gaugeMetric = GaugeMetric(gaugeName, timestamp = None, 2.0, 0.0, 42.0)
    val gaugeMetric2 = GaugeMetric(gaugeName, timestamp = None, 2.1, 0.0, 42.0)

    val metrics = Seq(counterMetric,gaugeMetric,gaugeMetric2)
    val otherMetrics = Seq(other)
    val collection = MetricsCollection(metrics)
    val otherCollection = MetricsCollection(otherMetrics)

    "should return the correct CounterMetric for the find operator" in {
      val cm = collection.find(counterMetricDesc, uuid)
      cm shouldNot be (empty)
      cm.get.value should equal (1.0)
    }

    "should return the first GaugeMetric of 2 with identical names" in {
      val gm = collection.find(gaugeMetricDesc, uuid)
      gm shouldNot be (empty)
      gm.get.value shouldNot be (2.1)
      gm.get.value should be (2.0)
    }

    "should return None for a metric that is not in the collection" in {
      val nm = collection.find(falseDesc, uuid)
      nm should be (empty)
    }

    "should combine two MetricCollections" in {
      val nc = collection ++ otherCollection
      val om = nc.find(otherDesc, uuid)
      nc.metrics.size should be (4)
      om.get.value should be (0.1)
    }

    "should return the correct Metric with the general find approach" in {
      val cm = collection.find[CounterMetric](counterName)
      cm shouldNot be (empty)
      cm.get.value should be (1.0)
    }
  }

}

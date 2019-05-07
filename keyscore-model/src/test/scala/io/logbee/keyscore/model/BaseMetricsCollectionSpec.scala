package io.logbee.keyscore.model

import io.logbee.keyscore.model.data.{Label, TextValue}
import io.logbee.keyscore.model.metrics._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class BaseMetricsCollectionSpec extends FreeSpec with Matchers {

  "A MetricsCollection" - {

    val inLabel = Label("port", TextValue("in"))
    val outLabel = Label("port", TextValue("out"))

    val otherName = "_"
    val otherDesc = CounterMetricDescriptor(otherName)
    val other = CounterMetric(otherName, Set(), timestamp = None, 0.1)
    val falseDesc = CounterMetricDescriptor("__")

    val counterName ="_counter"
    val counterMetricDesc = CounterMetricDescriptor(counterName)
    val counterMetric = CounterMetric(counterName, Set(inLabel), timestamp = None, 1.0)

    val gaugeName = "_gauge"
    val gaugeMetricDesc = GaugeMetricDescriptor(gaugeName)

    val gaugeMetric = GaugeMetric(gaugeName, Set(inLabel), timestamp = None, 2.0, 0.0, 42.0)
    val gaugeMetric2 = GaugeMetric(gaugeName, Set(outLabel), timestamp = None, 2.1, 0.0, 42.0)

    val metrics = List(counterMetric,gaugeMetric,gaugeMetric2)
    val otherMetrics = List(other)
    val collection = MetricsCollection(metrics)
    val otherCollection = MetricsCollection(otherMetrics)

    "should return the correct CounterMetric for the find operator" in {
      val cm = collection.find(counterMetricDesc)
      cm shouldNot be (empty)
      cm.get.value should equal (1.0)
    }

    "should return the first GaugeMetric of 2 with identical names" in {
      val gm = collection.find(gaugeMetricDesc)
      gm shouldNot be (empty)
      gm.get.value shouldNot be (2.1)
      gm.get.value should be (2.0)
    }

    "should return None for a metric that is not in the collection" in {
      val nm = collection.find(falseDesc)
      nm should be (empty)
    }

    "should combine two MetricCollections" in {
      val nc = collection ++ otherCollection
      val om = nc.find(otherDesc)
      nc.metrics.size should be (4)
      om.get.value should be (0.1)
    }

    "should return the correct Metric with the general find approach" in {
      val cm = collection.find[CounterMetric](counterName)
      cm shouldNot be (empty)
      cm.get.value should be (1.0)

      val in = collection.find[GaugeMetric](gaugeName, Set(inLabel))
      in shouldNot be (empty)
      in.get.value should be (2.0)
    }

    "should return all metrics with the same name" in {
      val col = collection.findMetrics[GaugeMetric](gaugeName)
      col.size should be (2)
      col.head.value should be (2.0)
      col.last.value should be (2.1)

      val in = collection.findMetrics[GaugeMetric](gaugeName, Set(outLabel))
      in.size should be (1)
      in.head.value should be (2.1)
    }

    "should return all metrics with the same labels" in {
      val in = collection.findMetricsWithLabels[GaugeMetric](Set(inLabel))
      in.size should be (1)
      in.head.value should be (2.0)
    }
  }

}

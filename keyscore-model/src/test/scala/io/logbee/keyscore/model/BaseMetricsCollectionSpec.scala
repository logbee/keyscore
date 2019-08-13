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
    val other = CounterMetric(otherName, Set(), timestamp = None, value = 1)
    val falseDesc = CounterMetricDescriptor("__")

    val counterName ="_counter"
    val counterMetricDesc = CounterMetricDescriptor(counterName)
    val counterMetric = CounterMetric(counterName, Set(inLabel), timestamp = None, value = 1)

    val gaugeName = "_gauge"
    val gaugeMetricDesc = NumberGaugeMetricDescriptor(gaugeName)

    val gaugeMetric = NumberGaugeMetric(gaugeName, Set(inLabel), timestamp = None, value = 2)
    val gaugeMetric2 = NumberGaugeMetric(gaugeName, Set(outLabel), timestamp = None, value = 21)

    val metrics = List(counterMetric,gaugeMetric,gaugeMetric2)
    val otherMetrics = List(other)
    val collection = MetricsCollection(metrics)
    val otherCollection = MetricsCollection(otherMetrics)

    "should return the correct CounterMetric for the find operator" in {
      val cm = collection.find(counterMetricDesc)
      cm shouldNot be (empty)
      cm.get.value should equal (1)
    }

    "should return the first GaugeMetric of 2 with identical names" in {
      val gm = collection.find(gaugeMetricDesc)
      gm shouldNot be (empty)
      gm.get.value shouldNot be (21)
      gm.get.value should be (2)
    }

    "should return None for a metric that is not in the collection" in {
      val nm = collection.find(falseDesc)
      nm should be (empty)
    }

    "should combine two MetricCollections" in {
      val nc = collection ++ otherCollection
      val om = nc.find(otherDesc)
      nc.metrics.size should be (4)
      om.get.value should be (1)
    }

    "should return the correct Metric with the general find approach" in {
      val cm = collection.find[CounterMetric](counterName)
      cm shouldNot be (empty)
      cm.get.value should be (1)

      val in = collection.find[NumberGaugeMetric](gaugeName, Set(inLabel))
      in shouldNot be (empty)
      in.get.value should be (2)
    }

    "should return all metrics with the same name" in {
      val col = collection.findMetrics[NumberGaugeMetric](gaugeName)
      col.size should be (2)
      col.head.value should be (2)
      col.last.value should be (21)

      val in = collection.findMetrics[NumberGaugeMetric](gaugeName, Set(outLabel))
      in.size should be (1)
      in.head.value should be (21)
    }

    "should return all metrics with the same labels" in {
      val in = collection.findMetricsWithLabels[NumberGaugeMetric](Set(inLabel))
      in.size should be (1)
      in.head.value should be (2)
    }
  }
}

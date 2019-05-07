package io.logbee.keyscore.pipeline.api.metrics
import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.model.data.{Label, TimestampValue}
import io.logbee.keyscore.model.metrics._

import scala.collection.mutable

class DefaultMetricsCollector() extends MetricsCollector {

  private val metrics = mutable.HashMap.empty[String, Metric]

  override def collect(descriptor: CounterMetricDescriptor): CounterMetricCollector = new CounterMetricCollector {

    private val name: String = descriptor.name

    override def reset(): CounterMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.value := 0,
        _.timestamp := now
      ))
      this
    }

    override def increment(): CounterMetricCollector = {
      val metric = getOrCreate()
      metrics.update(name, metric.update(
        _.value := metric.value + 1,
        _.timestamp := now
      ))
      this
    }

    private def getOrCreate(): CounterMetric =
      metrics.getOrElse(name, CounterMetric(name))
        .asInstanceOf[CounterMetric]
  }

  override def collect(descriptor: GaugeMetricDescriptor): GaugeMetricCollector = new GaugeMetricCollector {

    private val name: String = descriptor.name

    override def set(value: Double): GaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.value := value,
        _.timestamp := now
      ))
      this
    }

    override def increment(amount: Double = 1.0): GaugeMetricCollector = {
      val metric = getOrCreate()
      metrics.update(name, metric.update(
        _.value := metric.value + amount,
        _.timestamp := now
      ))
      this
    }

    override def decrement(amount: Double = 1.0): GaugeMetricCollector = {
      val metric = getOrCreate()
      metrics.update(name, metric.update(
        _.value := metric.value - amount,
        _.timestamp := now
      ))
      this
    }

    override def min(value: Double): GaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.min := value,
        _.timestamp := now
      ))
      this
    }

    override def max(value: Double): GaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.max := value,
        _.timestamp := now
      ))
      this
    }

    private def getOrCreate(): GaugeMetric =
      metrics.getOrElse(name, GaugeMetric(name))
      .asInstanceOf[GaugeMetric]
  }

  def get: MetricsCollection = MetricsCollection(metrics.values.toList)

  def getWithLabels(labels: Set[Label]): MetricsCollection = {
    MetricsCollection(metrics.values.map {
      case metric @ CounterMetric(_, _, _, _) => metric.update(_.labels :++= labels)
      case metric @ GaugeMetric(_, _, _, _, _, _) => metric.update(_.labels :++= labels)
    }.toSeq)
  }

  private def now: TimestampValue = {
    val now = Timestamps.fromMillis(System.currentTimeMillis())
    TimestampValue(now.getSeconds, now.getNanos)
  }
}

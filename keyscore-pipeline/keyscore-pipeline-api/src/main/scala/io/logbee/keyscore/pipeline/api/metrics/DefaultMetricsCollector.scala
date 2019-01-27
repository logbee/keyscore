package io.logbee.keyscore.pipeline.api.metrics
import java.util.UUID

import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.model.data.TimestampValue
import io.logbee.keyscore.model.metrics._

import scala.collection.mutable

class DefaultMetricsCollector(uuid: UUID) extends MetricsCollector {

  private val metrics = mutable.HashMap.empty[String, Metric]

  override def collect(descriptor: CounterMetricDescriptor): CounterMetricCollector = new CounterMetricCollector {

    override def reset(): CounterMetricCollector = {
      metrics.update(descriptor.name, getOrCreate().update(
        _.value := 0,
        _.timestamp := now
      ))
      this
    }

    override def increment(): CounterMetricCollector = {
      val metric = getOrCreate()
      metrics.update(descriptor.name, metric.update(
        _.value := metric.value + 1,
        _.timestamp := now
      ))
      this
    }

    private def getOrCreate(): CounterMetric =
      metrics.getOrElse(descriptor.name, GaugeMetric(descriptor.name))
        .asInstanceOf[CounterMetric]
  }

  override def collect(descriptor: GaugeMetricDescriptor): GaugeMetricCollector = new GaugeMetricCollector {

    override def set(value: Double): GaugeMetricCollector = {
      metrics.update(descriptor.name, getOrCreate().update(
        _.value := value,
        _.timestamp := now
      ))
      this
    }

    override def increment(amount: Double = 1.0): GaugeMetricCollector = {
      val metric = getOrCreate()
      metrics.update(descriptor.name, metric.update(
        _.value := metric.value + amount,
        _.timestamp := now
      ))
      this
    }

    override def decrement(amount: Double = 1.0): GaugeMetricCollector = {
      val metric = getOrCreate()
      metrics.update(descriptor.name, metric.update(
        _.value := metric.value - amount,
        _.timestamp := now
      ))
      this
    }

    override def min(value: Double): GaugeMetricCollector = {
      metrics.update(descriptor.name, getOrCreate().update(
        _.min := value,
        _.timestamp := now
      ))
      this
    }

    override def max(value: Double): GaugeMetricCollector = {
      metrics.update(descriptor.name, getOrCreate().update(
        _.max := value,
        _.timestamp := now
      ))
      this
    }

    private def getOrCreate(): GaugeMetric =
      metrics.getOrElse(descriptor.name, GaugeMetric(descriptor.name))
      .asInstanceOf[GaugeMetric]
  }

  def get: MetricsCollection = MetricsCollection(uuid.toString, now, metrics.values.toSeq)

  private def now: TimestampValue = {
    val now = Timestamps.fromMillis(System.currentTimeMillis())
    TimestampValue(now.getSeconds, now.getNanos)
  }
}

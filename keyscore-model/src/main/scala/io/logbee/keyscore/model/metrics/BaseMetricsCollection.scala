package io.logbee.keyscore.model.metrics

import java.util.UUID

import scala.reflect.ClassTag

trait BaseMetricsCollection {
  this: MetricsCollection =>

  def find(descriptor: CounterMetricDescriptor, id: UUID): Option[CounterMetric] = {
    val metricName: String = s"$id.${descriptor.name}"
    metrics.filter(_.isInstanceOf[CounterMetric]).map(_.asInstanceOf[CounterMetric]).find(cm => cm.name == metricName)
  }

  def find(descriptor: GaugeMetricDescriptor, id: UUID): Option[GaugeMetric] = {
    val metricName: String = s"$id.${descriptor.name}"
    metrics.filter(_.isInstanceOf[GaugeMetric]).map(_.asInstanceOf[GaugeMetric]).find(cm => cm.name == metricName)
  }

  def find[T <: Metric](name: String)(implicit classTag: ClassTag[T]): Option[T] = {
    metrics.find(metric => {
      classTag.runtimeClass.equals(metric.getClass) && name.equals(metric.asMessage.name)
    }).map(m => m.asInstanceOf[T])
  }

  def ++(other: MetricsCollection): MetricsCollection = {
    MetricsCollection(
      metrics = this.metrics ++ other.metrics
    )
  }
}

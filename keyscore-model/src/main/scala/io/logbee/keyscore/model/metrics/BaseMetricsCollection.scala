package io.logbee.keyscore.model.metrics

import io.logbee.keyscore.model.data.Label

import scala.reflect.ClassTag

trait BaseMetricsCollection {
  this: MetricsCollection =>

  def find(descriptor: CounterMetricDescriptor): Option[CounterMetric] = {
    val metricName: String = descriptor.name
    metrics.filter(_.isInstanceOf[CounterMetric]).map(_.asInstanceOf[CounterMetric]).find(cm => cm.name == metricName)
  }

  def find(descriptor: GaugeMetricDescriptor): Option[GaugeMetric] = {
    val metricName: String = descriptor.name
    metrics.filter(_.isInstanceOf[GaugeMetric]).map(_.asInstanceOf[GaugeMetric]).find(cm => cm.name == metricName)
  }

  def find[T <: Metric](name: String, labels: Set[Label] = Set.empty)(implicit classTag: ClassTag[T]): Option[T] = {
    metrics.find(metric => {
      classTag.runtimeClass.equals(metric.getClass) && name.equals(metric.asMessage.name) && labels.forall(metric.asMessage.labels.contains)
    }).map(m => m.asInstanceOf[T])
  }

  def findMetrics[T <: Metric](name: String, labels: Set[Label] = Set.empty)(implicit classTag: ClassTag[T]): List[T] = {
    metrics.filter(metric => {
      classTag.runtimeClass.equals(metric.getClass) && name.equals(metric.asMessage.name) && labels.forall(metric.asMessage.labels.contains)
    }).map(m => m.asInstanceOf[T])
  }

  def findMetricsWithLabels[T <: Metric](labels: Set[Label] = Set.empty)(implicit classTag: ClassTag[T]): List[T] = {
    metrics.filter(metric => {
      classTag.runtimeClass.equals(metric.getClass) && labels.forall(metric.asMessage.labels.contains)
    }).map(m => m.asInstanceOf[T])
  }

  def ++(other: MetricsCollection): MetricsCollection = {
    MetricsCollection(
      metrics = this.metrics ++ other.metrics
    )
  }


}

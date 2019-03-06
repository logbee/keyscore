package io.logbee.keyscore.model.metrics

import java.util.UUID

trait BaseMetricsCollection {
  this: MetricsCollection =>

  def find(descriptor: CounterMetricDescriptor, valveID: UUID): CounterMetric = {
    val metricName: String = s"$valveID.${descriptor.name}"
    metrics.filter(_.isInstanceOf[CounterMetric]).map(_.asInstanceOf[CounterMetric]).find(cm => cm.name == metricName).get
  }

  def find(descriptor: GaugeMetricDescriptor, valveID: UUID): GaugeMetric = {
    val metricName: String = s"$valveID.${descriptor.name}"
    metrics.filter(_.isInstanceOf[GaugeMetric]).map(_.asInstanceOf[GaugeMetric]).find(cm => cm.name == metricName).get
  }

  def ++(other: MetricsCollection): MetricsCollection = {
    MetricsCollection(
      metrics = this.metrics ++ other.metrics
    )
  }
}

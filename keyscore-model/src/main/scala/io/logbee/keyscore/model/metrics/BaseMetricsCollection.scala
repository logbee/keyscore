package io.logbee.keyscore.model.metrics

trait BaseMetricsCollection {
  this: MetricsCollection =>

  def ++(other: MetricsCollection): MetricsCollection = {
    MetricsCollection(
      metrics = this.metrics ++ other.metrics
    )
  }
}

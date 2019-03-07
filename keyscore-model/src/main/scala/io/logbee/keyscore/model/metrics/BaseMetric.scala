package io.logbee.keyscore.model.metrics

trait BaseMetric {
  this: MetricMessage =>

  def name: String = {
    sealedValue.value match {
      case CounterMetric(name, _, _) => name
      case GaugeMetric(name, _, _, _, _) => name
      case _  => ""
    }
  }
}

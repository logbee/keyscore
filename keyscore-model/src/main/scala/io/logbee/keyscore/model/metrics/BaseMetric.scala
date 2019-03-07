package io.logbee.keyscore.model.metrics

import io.logbee.keyscore.model.data.Label

trait BaseMetric {
  this: MetricMessage =>

  def name: String = {
    sealedValue.value match {
      case CounterMetric(name, _, _, _) => name
      case GaugeMetric(name, _, _, _, _, _) => name
      case _  => ""
    }
  }

  def labels: Set[Label] = {
    sealedValue.value match {
      case CounterMetric(_, labelsSet, _, _) => labelsSet
      case GaugeMetric(_, labelsSet, _, _, _, _) => labelsSet
      case _  => Set.empty
    }
  }
}

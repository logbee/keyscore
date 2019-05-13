package io.logbee.keyscore.model.metrics

import io.logbee.keyscore.model.data.{Label, TimestampValue}

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

  def timestamp: TimestampValue = {
    sealedValue.value match {
      case CounterMetric(_, _, timestamp, _) => timestamp.get
      case GaugeMetric(_, _, timestamp, _, _, _) => timestamp.get
      case _ => TimestampValue()
    }
  }
}

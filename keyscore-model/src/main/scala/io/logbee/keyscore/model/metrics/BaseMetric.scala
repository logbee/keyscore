package io.logbee.keyscore.model.metrics

import io.logbee.keyscore.model.data.{Label, TimestampValue}

trait BaseMetric {
  this: MetricMessage =>

  def value: Double = {
    sealedValue.value match {
      case CounterMetric(_, _, _, value) => value
      case GaugeMetric(_, _, _, value, _, _) => value
      case _  => Double.MinValue
    }
  }

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

  def typ: MetricTyp = {
    sealedValue.value match {
      case CounterMetric(_, _, _, _) => CounterMetricTyp
      case GaugeMetric(_, _, _, _, _, _) => GaugeMetricTyp
      case _  => UnknownMetricTyp
    }
  }

  def min: Option[Double] = {
    sealedValue.value match {
      case CounterMetric(_, _, _, _) => None
      case GaugeMetric(_, _, _, _, min, _) => Some(min)
      case _ => None
    }
  }

  def max: Option[Double] = {
    sealedValue.value match {
      case CounterMetric(_, _, _, _) => None
      case GaugeMetric(_, _, _, _, _, max) => Some(max)
      case _ => None
    }
  }
}

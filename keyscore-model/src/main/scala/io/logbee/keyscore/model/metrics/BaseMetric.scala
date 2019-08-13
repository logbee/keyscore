package io.logbee.keyscore.model.metrics

import io.logbee.keyscore.model.data.{Label, NumberValue, TimestampValue}

trait BaseMetric { this: MetricMessage =>

  def name: String = {
    sealedValue.value match {
      case CounterMetric(name, _, _, _, _) => name
      case NumberGaugeMetric(name, _, _, _, _, _, _) => name
      case DecimalGaugeMetric(name, _, _, _, _, _, _) => name
      case _  => ""
    }
  }

  def labels: Set[Label] = {
    sealedValue.value match {
      case CounterMetric(_, labelsSet, _, _, _) => labelsSet
      case NumberGaugeMetric(_, labelsSet, _, _, _, _, _) => labelsSet
      case DecimalGaugeMetric(_, labelsSet, _, _, _, _, _) => labelsSet
      case _  => Set.empty
    }
  }

  def timestamp: TimestampValue = {
    sealedValue.value match {
      case CounterMetric(_, _, timestamp, _, _) => timestamp.get
      case NumberGaugeMetric(_, _, timestamp, _, _, _, _) => timestamp.get
      case DecimalGaugeMetric(_, _, timestamp, _, _, _, _) => timestamp.get
      case _ => TimestampValue()
    }
  }

  def timedelta: NumberValue = {
    sealedValue.value match {
      case CounterMetric(_, _, _, timedelta, _) => timedelta
      case NumberGaugeMetric(_, _, _, timedelta, _, _, _) => timedelta
      case DecimalGaugeMetric(_, _, _, timedelta, _, _, _) => timedelta
      case _ => NumberValue()
    }
  }

  def typ: MetricType = {
    sealedValue.value match {
      case CounterMetric(_, _, _, _, _) => CounterMetricType
      case NumberGaugeMetric(_, _, _, _, _, _, _) => NumberGaugeMetricType
      case DecimalGaugeMetric(_, _, _, _, _, _, _) => DecimalGaugeMetricType
      case _  => UnknownMetricType
    }
  }
}

package io.logbee.keyscore.model.metrics


sealed trait MetricTyp
case object GaugeMetricTyp extends MetricTyp
case object CounterMetricTyp extends MetricTyp
case object UnknownMetricTyp extends MetricTyp

object MetricAttributes {
  val METRIC_TYP: String = "_keyscore_metric_typ"
  val METRIC_NAME: String = "_keyscore_metric_name"
  val METRIC_VALUE: String = "_keyscore_metric_value"
  val METRIC_TIMESTAMP: String = "_keyscore_metric_timestamp"
  val METRIC_MAX: String = "_keyscore_metric_min"
  val METRIC_MIN: String = "_keyscore_metric_max"
}
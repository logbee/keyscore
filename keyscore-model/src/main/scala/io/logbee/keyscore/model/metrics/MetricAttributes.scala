package io.logbee.keyscore.model.metrics


sealed trait MetricTyp

case object CounterMetricTyp extends MetricTyp
case object NumberGaugeMetricTyp extends MetricTyp
case object DecimalGaugeMetricTyp extends MetricTyp
case object UnknownMetricTyp extends MetricTyp

object MetricAttributes {
  /**
    * Specifies the ID (eg. filter or agent)
    */
  val METRIC_ID: String = "metric.id"
  val METRIC_TYP: String = "metric.typ"
  val METRIC_NAME: String = "metric.name"
  val METRIC_VALUE: String = "metric.value"
  val METRIC_TIMESTAMP: String = "metric.timestamp"
  val METRIC_MIN: String = "metric.min"
  val METRIC_MAX: String = "metric.max"
}
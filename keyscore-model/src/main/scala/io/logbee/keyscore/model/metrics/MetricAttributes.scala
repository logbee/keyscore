package io.logbee.keyscore.model.metrics


sealed trait MetricType

case object CounterMetricType extends MetricType
case object NumberGaugeMetricType extends MetricType
case object DecimalGaugeMetricType extends MetricType
case object UnknownMetricType extends MetricType

object MetricAttributes {
  /**
    * Specifies the ID (eg. filter or agent)
    */
  val METRIC_ID: String = "filter.id"
  val METRIC_TYP: String = "metric.type"
  val METRIC_NAME: String = "metric.name"
  val METRIC_VALUE: String = "metric.value"
  val METRIC_TIMESTAMP: String = "metric.timestamp"
  val METRIC_MIN: String = "metric.min"
  val METRIC_MAX: String = "metric.max"
}
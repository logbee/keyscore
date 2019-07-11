package io.logbee.keyscore.model.metrics

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, OffsetDateTime, ZoneId}

import io.logbee.keyscore.model.data._

object MetricConversion {

  def labelToField(label: Label): Field = {
    Field(label.name, label.value)
  }

  def metricToRecord(metric: Metric, id: String): Record = {

    val idField = Field(MetricAttributes.METRIC_ID, TextValue(id))
    val typField = Field(MetricAttributes.METRIC_TYP, TextValue(metric.asMessage.typ.toString))
    val nameField = Field(MetricAttributes.METRIC_NAME, TextValue(metric.asMessage.name))
    val timestamp = Field(MetricAttributes.METRIC_TIMESTAMP, TimestampValue(metric.asMessage.timestamp))
    val timedelta = Field(MetricAttributes.METRIC_TIMEDELTA, metric.asMessage.timedelta)

    val metricValues = metric match {
      case CounterMetric(_, _, _, _, value) => List(Field(MetricAttributes.METRIC_VALUE, NumberValue(value)))
      case NumberGaugeMetric(_, _, _, _, value, min, max) =>
        List(
          Field(MetricAttributes.METRIC_VALUE, NumberValue(value)),
          Field(MetricAttributes.METRIC_MIN, NumberValue(min)),
          Field(MetricAttributes.METRIC_MAX, NumberValue(max))
        )
      case DecimalGaugeMetric(_, _, _, _, value, min, max) =>
        List(
          Field(MetricAttributes.METRIC_VALUE, DecimalValue(value)),
          Field(MetricAttributes.METRIC_MIN, DecimalValue(min)),
          Field(MetricAttributes.METRIC_MAX, DecimalValue(max))
        )
    }

    val labels: List[Field] = metric.asMessage.labels.map(labelToField).toList

    Record(List(idField, typField, nameField, timestamp, timedelta) ++ metricValues ++ labels)
  }

  def convertMetricCollectionToDataset(id: String, mc: MetricsCollection): Dataset = {
    val records = mc.metrics.map { m =>
      metricToRecord(m, id)
    }
    Dataset(records)
  }

  def getLatest(mc: MetricsCollection): TimestampValue = {
    mc.metrics.map { metric => metric.asMessage.timestamp }.reduceLeft(latest)
  }

  def getEarliest(mc: MetricsCollection): TimestampValue = {
    mc.metrics.map { metric => metric.asMessage.timestamp }.reduce(earliest)
  }

  private def latest(first: TimestampValue, second: TimestampValue): TimestampValue = {
    if (first.seconds < second.seconds) second
    else if (first.seconds == second.seconds) {
      if (first.nanos < second.nanos) second
      else first
    }
    else first
  }

  private def earliest(first: TimestampValue, second: TimestampValue): TimestampValue = {
    if (first.seconds > second.seconds) second
    else if (first.seconds == second.seconds) {
      if (first.nanos > second.seconds) second
      else first
    }
    else first
  }

  def timestampToString(tsv: TimestampValue, format: String): String = {
    val localDateTime = LocalDateTime.ofEpochSecond(tsv.seconds, tsv.nanos, OffsetDateTime.now(ZoneId.systemDefault()).getOffset)

    localDateTime.format(DateTimeFormatter.ofPattern(format))
  }

}

package io.logbee.keyscore.model.metrics

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, OffsetDateTime, ZoneId}

import io.logbee.keyscore.model.data._

object MetricConversion {

  def labelToField(label: Label): Field = {
    Field(label.name, label.value)
  }

  def metricToRecord(metric: Metric, id: String): Record = {

    val typ = metric.asMessage.typ

    val idField = Field(MetricAttributes.METRIC_ID, TextValue(id))
    val typField = Field(MetricAttributes.METRIC_TYP, TextValue(typ.toString))
    val nameField = Field(MetricAttributes.METRIC_NAME, TextValue(metric.asMessage.name))
    val valueField = Field(MetricAttributes.METRIC_VALUE, DecimalValue(metric.asMessage.value))
    val timestamp = Field(MetricAttributes.METRIC_TIMESTAMP, TimestampValue(metric.asMessage.timestamp))

    val labels: List[Field] = metric.asMessage.labels.map(labelToField).toList

    val gaugeFields: List[Field] = List((MetricAttributes.METRIC_MIN, metric.asMessage.min), (MetricAttributes.METRIC_MAX, metric.asMessage.max)).collect {
      case (name, v: Some[Double]) => Field(name, DecimalValue(v.value))
    }

    Record(List(idField, typField, nameField, valueField, timestamp) ++ gaugeFields ++ labels)

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

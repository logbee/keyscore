package io.logbee.keyscore.model.metrics

import io.logbee.keyscore.model.data._

object MetricConversion {

  def labelToField(label: Label): Field = {
    Field(label.name, label.value)
  }

  def metricToRecord(metric: Metric): Record = {

    val typ = metric.asMessage.typ

    val typField = Field(MetricAttributes.METRIC_TYP, TextValue(typ.toString))
    val nameField = Field(MetricAttributes.METRIC_NAME, TextValue(metric.asMessage.name))
    val valueField = Field(MetricAttributes.METRIC_VALUE, DecimalValue(metric.asMessage.value))
    val timestamp = Field(MetricAttributes.METRIC_TIMESTAMP, TimestampValue(metric.asMessage.timestamp))

    val labels: List[Field] = metric.asMessage.labels.map(labelToField).toList

    val gaugeFields: List[Field] = List((MetricAttributes.METRIC_MIN, metric.asMessage.min), (MetricAttributes.METRIC_MAX, metric.asMessage.max)).collect {
      case (name, v: Some[Double]) => Field(name, DecimalValue(v.value))
    }

    Record(List(typField, nameField, valueField, timestamp) ++ gaugeFields ++ labels)

  }

  def convertMetricCollectionToDataset(id: String, mc: MetricsCollection): Dataset = {
    val records = mc.metrics.map { metricToRecord }
    Dataset(metadata = MetaData(Set(Label(MetricAttributes.METRIC_COLLECTION_ID, TextValue(id)))), records)
  }

  def getLatest(mc: MetricsCollection): TimestampValue = {
    mc.metrics.map{ metric => metric.asMessage.timestamp }.reduceLeft(latest)
  }

  def getEarliest(mc: MetricsCollection): TimestampValue = {
    mc.metrics.map { metric => metric.asMessage.timestamp }.reduce(earliest)
  }

  private def latest(first: TimestampValue, second: TimestampValue): TimestampValue = {
    if(first.seconds < second.seconds) second
    else if (first.seconds == second.seconds) {
      if(first.nanos < second.nanos) second
      else first
    }
    else first
  }

  private def earliest(first: TimestampValue, second: TimestampValue): TimestampValue = {
    if (first.seconds > second.seconds) second
    else if(first.seconds == second.seconds) {
      if (first.nanos > second.seconds) second
      else first
    }
    else first
  }

}

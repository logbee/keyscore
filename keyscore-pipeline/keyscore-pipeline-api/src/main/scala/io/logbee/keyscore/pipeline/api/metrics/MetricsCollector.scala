package io.logbee.keyscore.pipeline.api.metrics

import io.logbee.keyscore.model.metrics.{CounterMetricDescriptor, DecimalGaugeMetricDescriptor, MetricsCollection, NumberGaugeMetricDescriptor}

trait MetricsCollector {
  def collect(descriptor: CounterMetricDescriptor): CounterMetricCollector
  def collect(descriptor: NumberGaugeMetricDescriptor): NumberGaugeMetricCollector
  def collect(descriptor: DecimalGaugeMetricDescriptor): DecimalGaugeMetricCollector
  def get: MetricsCollection
  def scrape: MetricsCollection
}

trait CounterMetricCollector {
  def reset(): CounterMetricCollector
  def increment(): CounterMetricCollector
}

trait NumberGaugeMetricCollector {
  def set(value: Long): NumberGaugeMetricCollector
  def increment(amount: Long = 1L): NumberGaugeMetricCollector
  def decrement(amount: Long = 1L): NumberGaugeMetricCollector
  def min(value: Long): NumberGaugeMetricCollector
  def max(value: Long): NumberGaugeMetricCollector
}

trait DecimalGaugeMetricCollector {
  def set(value: Double): DecimalGaugeMetricCollector
  def increment(amount: Double = 1.0): DecimalGaugeMetricCollector
  def decrement(amount: Double = 1.0): DecimalGaugeMetricCollector
  def min(value: Double): DecimalGaugeMetricCollector
  def max(value: Double): DecimalGaugeMetricCollector
}

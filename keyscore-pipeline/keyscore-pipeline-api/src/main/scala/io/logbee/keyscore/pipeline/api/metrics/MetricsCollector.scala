package io.logbee.keyscore.pipeline.api.metrics

import io.logbee.keyscore.model.metrics.{CounterMetricDescriptor, GaugeMetricDescriptor, MetricsCollection}

trait MetricsCollector {
  def collect(descriptor: CounterMetricDescriptor): CounterMetricCollector
  def collect(descriptor: GaugeMetricDescriptor): GaugeMetricCollector
  def get: MetricsCollection
}

trait CounterMetricCollector {
  def reset(): CounterMetricCollector
  def increment(): CounterMetricCollector
}

trait GaugeMetricCollector {
  def set(value: Double): GaugeMetricCollector
  def increment(amount: Double = 1.0): GaugeMetricCollector
  def decrement(amount: Double = 1.0): GaugeMetricCollector
  def min(value: Double): GaugeMetricCollector
  def max(value: Double): GaugeMetricCollector
}

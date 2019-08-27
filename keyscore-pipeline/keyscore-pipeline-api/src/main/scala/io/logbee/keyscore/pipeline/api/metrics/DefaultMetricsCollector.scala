package io.logbee.keyscore.pipeline.api.metrics

import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.model.data.{Label, TimestampValue}
import io.logbee.keyscore.model.metrics._

import scala.collection.mutable

class DefaultMetricsCollector() extends MetricsCollector {

  private val metrics = mutable.HashMap.empty[String, Metric]
  private var lastScape = System.currentTimeMillis()

  override def collect(descriptor: CounterMetricDescriptor): CounterMetricCollector = new CounterMetricCollector {

    private val name: String = descriptor.name

    override def reset(): CounterMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.value := 0,
        _.timestamp := now
      ))
      this
    }

    override def increment(): CounterMetricCollector = {
      val metric = getOrCreate()
      metrics.update(name, metric.update(
        _.value := metric.value + 1,
        _.timestamp := now
      ))
      this
    }

    private def getOrCreate(): CounterMetric =
      metrics.getOrElse(name, CounterMetric(name))
        .asInstanceOf[CounterMetric]
  }

  override def collect(descriptor: NumberGaugeMetricDescriptor): NumberGaugeMetricCollector = new NumberGaugeMetricCollector {

    private val name: String = descriptor.name

    override def set(value: Long): NumberGaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.value := value,
        _.timestamp := now
      ))
      this
    }

    override def increment(amount: Long = 1L): NumberGaugeMetricCollector = {
      val metric = getOrCreate()
      metrics.update(name, metric.update(
        _.value := metric.value + amount,
        _.timestamp := now
      ))
      this
    }

    override def decrement(amount: Long = 1L): NumberGaugeMetricCollector = {
      val metric = getOrCreate()
      metrics.update(name, metric.update(
        _.value := metric.value - amount,
        _.timestamp := now
      ))
      this
    }

    override def min(value: Long): NumberGaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.min := value,
        _.timestamp := now
      ))
      this
    }

    override def max(value: Long): NumberGaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.max := value,
        _.timestamp := now
      ))
      this
    }

    private def getOrCreate(): NumberGaugeMetric =
      metrics.getOrElse(name, NumberGaugeMetric(name))
      .asInstanceOf[NumberGaugeMetric]
  }

  override def collect(descriptor: DecimalGaugeMetricDescriptor): DecimalGaugeMetricCollector = new DecimalGaugeMetricCollector {

    private val name: String = descriptor.name

    override def set(value: Double): DecimalGaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.value := value,
        _.timestamp := now
      ))
      this
    }

    override def increment(amount: Double = 1.0): DecimalGaugeMetricCollector = {
      val metric = getOrCreate()
      metrics.update(name, metric.update(
        _.value := metric.value + amount,
        _.timestamp := now
      ))
      this
    }

    override def decrement(amount: Double = 1.0): DecimalGaugeMetricCollector = {
      val metric = getOrCreate()
      metrics.update(name, metric.update(
        _.value := metric.value - amount,
        _.timestamp := now
      ))
      this
    }

    override def min(value: Double): DecimalGaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.min := value,
        _.timestamp := now
      ))
      this
    }

    override def max(value: Double): DecimalGaugeMetricCollector = {
      metrics.update(name, getOrCreate().update(
        _.max := value,
        _.timestamp := now
      ))
      this
    }

    private def getOrCreate(): DecimalGaugeMetric =
      metrics.getOrElse(name, DecimalGaugeMetric(name))
        .asInstanceOf[DecimalGaugeMetric]
  }

  def get: MetricsCollection = MetricsCollection(metrics.values.toList)

  private def calcDelta: Long = {
    val current = System.currentTimeMillis()
    val delta = current - lastScape
    lastScape = current
    delta
  }

  private def updateMetrics(): Unit = {
    val delta = calcDelta

    metrics.foreach {
      case (name, metric: NumberGaugeMetric) => metrics.update(name, metric.withValue(0).withTimestamp(__v = now).withTimedelta(__v = delta))
      case (name, metric: DecimalGaugeMetric) => metrics.update(name, metric.withValue(0).withTimestamp(__v = now).withTimedelta(__v = delta))
      case _ =>
    }
  }

  def scrape: MetricsCollection = {
    val result = MetricsCollection(metrics.values.toList)
    updateMetrics()
    result
  }

  def scrapeWithLabels(labels: Set[Label]): MetricsCollection = {
    val result = MetricsCollection(metrics.values.map {
      case metric : CounterMetric => metric.update(_.labels :++= labels)
      case metric : NumberGaugeMetric => metric.update(_.labels :++= labels)
      case metric : DecimalGaugeMetric => metric.update(_.labels :++= labels)
    }.toList)

    updateMetrics()
    result
  }

  private def now: TimestampValue = {
    val now = Timestamps.fromMillis(System.currentTimeMillis())
    TimestampValue(now.getSeconds, now.getNanos)
  }
}

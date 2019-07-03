package io.logbee.keyscore.pipeline.contrib

import io.logbee.keyscore.model.data.{Dataset, DecimalValue, Field, Label, Record, TextValue, TimestampValue, _}
import io.logbee.keyscore.model.metrics.MetricConversion._
import io.logbee.keyscore.model.metrics._
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class MetricSourceSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val addFieldsIdFromMetricsTest = "a2912661-7ce2-40d3-b490-d6c58a5cb70f"

  val cm = CounterMetric("cm3", value = 1.3, timestamp = Some(TimestampValue(9, 25)), labels = Set(Label("test", DecimalValue(42)), Label("x", TextValue("x"))))
  val gm = GaugeMetric("gm4", value = 1.4, timestamp = Some(TimestampValue(11, 27)), max = 42.0, labels = Set(Label("test2", DecimalValue(242)), Label("y", TextValue("y"))))
  val mcs = MetricsCollection(List(cm, gm))

  "A MetricSource" should {
    "convert Metric to Record" in {
      val cmr = metricToRecord(cm, addFieldsIdFromMetricsTest)
      val gmr = metricToRecord(gm, addFieldsIdFromMetricsTest)

      cmr shouldBe Record(List(Field("metric.id",TextValue(addFieldsIdFromMetricsTest)),Field("metric.typ", TextValue(CounterMetricTyp.toString)), Field("metric.name", TextValue("cm3")), Field("metric.value", DecimalValue(1.3)), Field("metric.timestamp", TimestampValue(9, 25)), Field("test", DecimalValue(42.0)), Field("x", TextValue("x"))))
      gmr shouldBe Record(List(Field("metric.id",TextValue(addFieldsIdFromMetricsTest)),Field("metric.typ", TextValue(GaugeMetricTyp.toString)), Field("metric.name", TextValue("gm4")), Field("metric.value", DecimalValue(1.4)), Field("metric.timestamp", TimestampValue(11, 27)), Field("metric.max", DecimalValue(0.0)), Field("metric.min", DecimalValue(42.0)), Field("test2", DecimalValue(242.0)), Field("y", TextValue("y"))))
    }

    "convert MetricCollection to Dataset" in {
      val dataset = Dataset(metadata = MetaData(),List(Record(List(Field("metric.id",TextValue(addFieldsIdFromMetricsTest)), Field("metric.typ",TextValue(CounterMetricTyp.toString)), Field("metric.name",TextValue("cm3")), Field("metric.value",DecimalValue(1.3)), Field("metric.timestamp",TimestampValue(9,25)), Field("test",DecimalValue(42.0)), Field("x",TextValue("x")))), Record(List(Field("metric.id",TextValue(addFieldsIdFromMetricsTest)), Field("metric.typ",TextValue(GaugeMetricTyp.toString)), Field("metric.name",TextValue("gm4")), Field("metric.value",DecimalValue(1.4)), Field("metric.timestamp",TimestampValue(11,27)), Field("metric.max",DecimalValue(0.0)), Field("metric.min",DecimalValue(42.0)), Field("test2",DecimalValue(242.0)), Field("y",TextValue("y"))))))
      convertMetricCollectionToDataset(addFieldsIdFromMetricsTest, mcs) shouldBe dataset
    }
  }
}
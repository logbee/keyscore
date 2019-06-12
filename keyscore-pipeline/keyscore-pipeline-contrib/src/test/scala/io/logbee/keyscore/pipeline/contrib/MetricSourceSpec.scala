package io.logbee.keyscore.pipeline.contrib

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import io.logbee.keyscore.commons.metrics.MetricsQuery
import io.logbee.keyscore.model.data.{DecimalValue, Field, Label, Record, TextValue, TimestampValue}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.metrics.{CounterMetric, GaugeMetric, GaugeMetricTyp, Metric, MetricAttributes, MetricsCollection}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.json4s.Formats
import org.json4s.native.Serialization.write
import org.junit.runner.RunWith
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.junit.JUnitRunner

import scala.collection.immutable
import scala.concurrent.Future

/**
  * Flaky test
  * Needs running Pipeline from MetricsTest (IntegrationTest)
  */
//@RunWith(classOf[JUnitRunner])
class MetricSourceSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  var server = "http://localhost"
  var port = 4711
  var limit = Long.MaxValue
  var format = "dd.MM.yyy_HH:mm:ss:nnnnnnnnn"
  var earliest = "01.01.2000_00:00:00:000000000"
  var latest = "31.12.9999_23:59:59:999999999"

  val addFieldsIdFromMetricsTest = "a2912661-7ce2-40d3-b490-d6c58a5cb70f"

  val mc1 = MetricsCollection(List(CounterMetric("cm3", value = 1.3, timestamp = Some(TimestampValue(9, 25))), GaugeMetric("gm4", value = 1.4, timestamp = Some(TimestampValue(11, 27)))))
  val mc2 = MetricsCollection(List(CounterMetric("cm33", value = 3.3, timestamp = Some(TimestampValue(9, 253))), GaugeMetric("gm44", value = 4.4, timestamp = Some(TimestampValue(11, 274)))))

  "A MetricSource" should {

//    "retrieve a sequence of MetricCollections" in {
//      implicit val formats: Formats = KeyscoreFormats.formats
//
//      val mq = MetricsQuery(limit, earliest, latest, format)
//
//      val response: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.POST, Uri(s"$server:$port/metrics/$addFieldsIdFromMetricsTest"), entity = HttpEntity(ContentTypes.`application/json`, write(mq))))
//
//      whenReady(response, interval = PatienceConfiguration.Interval(Span(1500, Milliseconds))) { res =>
//        val body = res.entity.asInstanceOf[HttpEntity.Strict].data.utf8String
//
//        val mcs: Seq[MetricsCollection] = org.json4s.native.Serialization.read[Seq[MetricsCollection]](body)
//
//        mcs shouldNot be(empty)
//      }
//    }

    "parse a MetricsCollection to a datasets" in {

//      def labelToField(label: Label): Field = {
//        Field(label.name, label.value)
//      }
//
//      def metricToRecord(metric: Metric): Record = {
//
//        val typ = metric.asMessage.typ
//
//        val typField = Field(MetricAttributes.METRIC_TYP, TextValue(typ.toString))
//        val nameField = Field(MetricAttributes.METRIC_NAME, TextValue(metric.asMessage.name))
//        val valueField = Field(MetricAttributes.METRIC_VALUE, DecimalValue(metric.asMessage.value))
//        val timestamp = Field(MetricAttributes.METRIC_TIMESTAMP, TimestampValue(metric.asMessage.timestamp))
//
//        val labels: List[Field] = metric.asMessage.labels.map(labelToField).toList
//
//        val gaugeFields: List[Field] = List((MetricAttributes.METRIC_MIN, metric.asMessage.min), (MetricAttributes.METRIC_MAX, metric.asMessage.max)).map { case (name: String, v: Some[Double]) => Field(name, DecimalValue(v.value))}
//
//        Record(List(typField, nameField, valueField, timestamp) ++ gaugeFields ++ labels)
//
//      }
//
//      val cm = CounterMetric("cm3", value = 1.3, timestamp = Some(TimestampValue(9, 25)), labels = Set(Label("test", DecimalValue(42)), Label("x", TextValue("x"))))
//      val gm = GaugeMetric("gm4", value = 1.4, timestamp = Some(TimestampValue(11, 27)), min = 1.0, max = 42.0, labels = Set(Label("test2", DecimalValue(242)), Label("y", TextValue("y"))))
//
//      val cmr = metricToRecord(cm)
//      val gmr = metricToRecord(gm)
//
//      println()

    }
  }

}

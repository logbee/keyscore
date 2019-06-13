package io.logbee.keyscore.pipeline.contrib

import io.logbee.keyscore.model.data.{Dataset, DecimalValue, Field, Label, Record, TextValue, TimestampValue, _}
import io.logbee.keyscore.model.metrics.MetricConversion._
import io.logbee.keyscore.model.metrics.{CounterMetric, CounterMetricTyp, GaugeMetric, GaugeMetricTyp, Metric, MetricAttributes, MetricConversion, MetricsCollection}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class MetricSourceSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val server = "http://localhost"
  val port = 4711
  val limit = Long.MaxValue
  val format = "dd.MM.yyy_HH:mm:ss:nnnnnnnnn"
  val earliest = "01.01.2000_00:00:00:000000000"
  val latest = "31.12.9999_23:59:59:999999999"

  val addFieldsIdFromMetricsTest = "a2912661-7ce2-40d3-b490-d6c58a5cb70f"

  val cm = CounterMetric("cm3", value = 1.3, timestamp = Some(TimestampValue(9, 25)), labels = Set(Label("test", DecimalValue(42)), Label("x", TextValue("x"))))
  val gm = GaugeMetric("gm4", value = 1.4, timestamp = Some(TimestampValue(11, 27)), max = 42.0, labels = Set(Label("test2", DecimalValue(242)), Label("y", TextValue("y"))))
  val mcs = MetricsCollection(List(cm, gm))

  "A MetricSource" should {
    /**
      * Flaky test
      * Needs running Pipeline from MetricsTest (IntegrationTest)
      */
//    "retrieve a sequence of MetricCollections" in {
//
//      import akka.http.scaladsl.Http
//      import akka.http.scaladsl.model._
//      import io.logbee.keyscore.commons.metrics.MetricsQuery
//      import io.logbee.keyscore.model.json4s.KeyscoreFormats
//      import org.json4s.Formats
//      import org.json4s.native.Serialization.write
//      import org.scalatest.time.{Milliseconds, Span}
//      import scala.concurrent.Future
//      import org.scalatest.concurrent.PatienceConfiguration
//      implicit val formats: Formats = KeyscoreFormats.formats
//
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

    "convert Metric to Record" in {
      val cmr = metricToRecord(cm)
      val gmr = metricToRecord(gm)

      cmr shouldBe Record(List(Field("_keyscore_metric_typ", TextValue(CounterMetricTyp.toString)), Field("_keyscore_metric_name", TextValue("cm3")), Field("_keyscore_metric_value", DecimalValue(1.3)), Field("_keyscore_metric_timestamp", TimestampValue(9, 25)), Field("test", DecimalValue(42.0)), Field("x", TextValue("x"))))
      gmr shouldBe Record(List(Field("_keyscore_metric_typ", TextValue(GaugeMetricTyp.toString)), Field("_keyscore_metric_name", TextValue("gm4")), Field("_keyscore_metric_value", DecimalValue(1.4)), Field("_keyscore_metric_timestamp", TimestampValue(11, 27)), Field("_keyscore_metric_max", DecimalValue(0.0)), Field("_keyscore_metric_min", DecimalValue(42.0)), Field("test2", DecimalValue(242.0)), Field("y", TextValue("y"))))
    }

    "convert MetricCollection to Dataset" in {
      val dataset = Dataset(MetaData(Label(MetricAttributes.METRIC_COLLECTION_ID, TextValue(addFieldsIdFromMetricsTest))), List(Record(List(Field("_keyscore_metric_typ",TextValue(CounterMetricTyp.toString)), Field("_keyscore_metric_name",TextValue("cm3")), Field("_keyscore_metric_value",DecimalValue(1.3)), Field("_keyscore_metric_timestamp",TimestampValue(9,25)), Field("test",DecimalValue(42.0)), Field("x",TextValue("x")))), Record(List(Field("_keyscore_metric_typ",TextValue(GaugeMetricTyp.toString)), Field("_keyscore_metric_name",TextValue("gm4")), Field("_keyscore_metric_value",DecimalValue(1.4)), Field("_keyscore_metric_timestamp",TimestampValue(11,27)), Field("_keyscore_metric_max",DecimalValue(0.0)), Field("_keyscore_metric_min",DecimalValue(42.0)), Field("test2",DecimalValue(242.0)), Field("y",TextValue("y"))))))
      convertMetricCollectionToDataset(addFieldsIdFromMetricsTest, mcs) shouldBe dataset
    }


  }

}

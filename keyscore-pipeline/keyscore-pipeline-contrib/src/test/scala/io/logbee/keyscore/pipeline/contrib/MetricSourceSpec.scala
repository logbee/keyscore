package io.logbee.keyscore.pipeline.contrib

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import io.logbee.keyscore.commons.metrics.MetricsQuery
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.json4s.Formats
import org.json4s.native.Serialization.write
import org.junit.runner.RunWith
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.junit.JUnitRunner

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

  "A MetricSource" should {

    "retrieve a sequence of MetricCollections" in {
      implicit val formats: Formats = KeyscoreFormats.formats

      val mq = MetricsQuery(limit, earliest, latest, format)

      val response: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.POST, Uri(s"$server:$port/metrics/$addFieldsIdFromMetricsTest"), entity = HttpEntity(ContentTypes.`application/json`, write(mq))))

      whenReady(response, interval = PatienceConfiguration.Interval(Span(1500, Milliseconds))) { res =>
        val body = res.entity.asInstanceOf[HttpEntity.Strict].data.utf8String

        val mcs: Seq[MetricsCollection] = org.json4s.native.Serialization.read[Seq[MetricsCollection]](body)

        mcs shouldNot be(empty)
      }
    }
  }

}

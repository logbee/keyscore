package io.logbee.keyscore.pipeline.contrib

import java.util.{Timer, TimerTask}

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.SourceShape
import io.logbee.keyscore.commons.metrics.MetricsQuery
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.metrics.{MetricConversion, MetricsCollection}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.{Formats, Serialization}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps

object MetricSourceLogic extends Described {

  val urlParameter = TextParameterDescriptor(
    ref = "metric.source.server",
    info = ParameterInfo(
      displayName = TextRef("bootstrapServer"),
      description = TextRef("serverDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "http://localhost:4711",
    mandatory = true
  )

  val idsParameter = TextListParameterDescriptor(
    "metric.source.ids",
    ParameterInfo(
      TextRef("ids"),
      TextRef("idsDescription")
    ),
    min = 1
  )

  val limitParameter = NumberParameterDescriptor(
    "metric.source.limit",
    ParameterInfo(
      TextRef("limit"),
      TextRef("limitDescription")),
    defaultValue = 10,
    range = NumberRange(step = 1, end = 10),
    mandatory = true
  )

  val earliestParameter = TextParameterDescriptor(
    ref = "metric.source.earliest",
    info = ParameterInfo(
      displayName = TextRef("earliest"),
      description = TextRef("earliestDescription")
    ),
    defaultValue = "01.01.2000 00:00:00",
    mandatory = true
  )

  val latestParameter = TextParameterDescriptor(
    ref = "metric.source.latest",
    info = ParameterInfo(
      displayName = TextRef("latest"),
      description = TextRef("latestDescription")
    ),
    defaultValue = "31.12.9999 23:59:59",
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "fd1db782-3d2f-467f-8dbf-9a928b092d7d",
    describes = SourceDescriptor(
      name = classOf[MetricSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.DEBUG),
      parameters = Seq(
        urlParameter,
        idsParameter,
        limitParameter,
        earliestParameter,
        latestParameter
      )
      //      ,
      //      icon = Icon.fromClass(classOf[MetricSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.MetricSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class MetricSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {

  import MetricSourceLogic._

  implicit private val formats: Formats = KeyscoreFormats.formats
  implicit val serialization: Serialization = Serialization

  private val format = "dd.MM.yyyy HH:mm:ss"

  private var lastTimePushed = System.currentTimeMillis()

  private var url = MetricSourceLogic.urlParameter.defaultValue
  private var ids = Seq.empty[String]
  private var limit = MetricSourceLogic.limitParameter.defaultValue
  private var earliest = MetricSourceLogic.earliestParameter.defaultValue
  private var latest = MetricSourceLogic.latestParameter.defaultValue

  private val metricCollections: ListBuffer[(String, MetricsCollection)] = ListBuffer.empty[(String, MetricsCollection)]

  private val idToEarliest = mutable.HashMap.empty[String, String]

  private val parseAsync = getAsyncCallback[(String, HttpResponse)]({ case (id, response) =>
    val mcs = parseHttpResponse(response)

    if (mcs.nonEmpty) {
      mcs.foreach { mc => metricCollections.append((id, mc)) }
      idToEarliest.update(id, getEarliest(mcs))
      tryPush()
    }
  })

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    scrapeMetrics()
    system.scheduler.schedule(30 seconds, 5 seconds)(checkLastTimePushed())
  }

  override def configure(configuration: Configuration): Unit = {
    setDefaults(configuration)
  }

  private def setDefaults(configuration: Configuration): Unit = {
    url = configuration.getValueOrDefault(urlParameter, url)
    ids = configuration.getValueOrDefault(idsParameter, ids)
    limit = configuration.getValueOrDefault(limitParameter, limit)
    earliest = configuration.getValueOrDefault(earliestParameter, earliest)
    latest = configuration.getValueOrDefault(latestParameter, latest)
  }

  override def onPull(): Unit = {
    log.debug("onPull")
    if (metricCollections.nonEmpty) {
      tryPush()
    }
    else {
      scrapeMetrics()
    }
  }

  private def tryPush(): Unit = {
    if (!isAvailable(out) && metricCollections.nonEmpty) {
      return
    } else if (metricCollections.isEmpty) {
      return
    }

    val metric = metricCollections.head

    push(out, MetricConversion.convertMetricCollectionToDataset(metric._1, metric._2))
    metricCollections -= metric
    lastTimePushed = System.currentTimeMillis()
  }

  private def scrapeMetrics(): Unit = {
    ids
      .map(_.trim)
      .map(id => {

        val newest = idToEarliest.getOrElse(id, earliest)
        val mq = MetricsQuery(limit, newest, latest, format)

        val uri = Uri(s"$url/metrics/$id")
        (id, Http().singleRequest(HttpRequest(HttpMethods.POST, uri, entity = HttpEntity(ContentTypes.`application/json`, write(mq)))))

      })
      .foreach({
        case (id, future) =>
          completeResponse(id, future)
      })
  }

  private def completeResponse(id: String, future: Future[HttpResponse]): Unit = {
    future.onComplete({
      case Success(response) =>
        parseAsync.invoke(id, response)
      case Failure(cause) =>
        log.debug(s"Couldn't retrieve metrics: $cause")
      case e =>
        log.debug(s"What's wrong: $e")
    })
  }

  private def parseHttpResponse(response: HttpResponse): Seq[MetricsCollection] = {
    if (response.status == StatusCodes.OK) {
      response.entity match {
        case strict: HttpEntity.Strict =>
          val body = strict.data.utf8String
          strict.discardBytes()
          org.json4s.native.Serialization.read[Seq[MetricsCollection]](body)
        case unknown =>
          unknown.discardBytes()
          Seq()
      }
    } else {
      log.debug(s"Failure: Response status is [${response.status}]")
      Seq()
    }
  }

  private def schedule(f: () => Unit, ms: Int): Unit = {
    val timer = new Timer
    val task = new TimerTask() {
      def run(): Unit = f()
    }
    timer.schedule(task, ms)
  }

  private def checkLastTimePushed(): Unit = {
    val now = System.currentTimeMillis()
    val dif = now - lastTimePushed
    if (dif > 10000) {
      log.info(s"Last time a dataset was pushed out was to long ago:  $dif ms")
      onPull()
    }
  }

  private def getEarliest(mcs: Seq[MetricsCollection]): String = {
    val timestampValue = MetricConversion.getLatest(mcs.head)
    MetricConversion.timestampToString(timestampValue, format)
  }
}

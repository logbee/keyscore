package io.logbee.keyscore.pipeline.contrib

import java.net.URL

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, SourceShape}
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
import scala.concurrent.Promise
import scala.util.{Failure, Success}

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
    defaultValue = 100,
    range = NumberRange(step = 1, end = Long.MaxValue),
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

  private var url = MetricSourceLogic.urlParameter.defaultValue
  private var ids = Seq.empty[String]
  private var limit = MetricSourceLogic.limitParameter.defaultValue
  private var earliest = MetricSourceLogic.earliestParameter.defaultValue
  private var latest = MetricSourceLogic.latestParameter.defaultValue

  private var queue:  SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] = _

  private val metricCollections: ListBuffer[(String, MetricsCollection)] = ListBuffer.empty[(String, MetricsCollection)]

  private val idToEarliest = mutable.HashMap.empty[String, String]

  private val parseAsync = getAsyncCallback[(String, HttpResponse)]({ case (id, response) =>
    val mcs = parseHttpResponse(response)
    mcs.foreach { mc => metricCollections.append((id, mc)) }

    if(mcs.nonEmpty) {
      idToEarliest.update(id, getEarliest(mcs))
      tryPush()
    }
  })

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    scrapeMetrics()
  }

  override def configure(configuration: Configuration): Unit = {
    setDefaults(configuration)

    val poolUrl = new URL(url)
    val pool = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = poolUrl.getHost, port = poolUrl.getPort)

    queue = Source.queue[(HttpRequest, Promise[HttpResponse])](1, OverflowStrategy.dropNew)
      .via(pool)
      .toMat(Sink.foreach({
        case (Success(response), promise) => promise.success(response)
        case (Failure(throwable), promise) => promise.failure(throwable)
      }))(Keep.left)
      .run()
  }
  override def onPull(): Unit = {
    if(metricCollections.nonEmpty) tryPush()
    else scrapeMetrics()
  }

  private def setDefaults(configuration: Configuration): Unit = {
    url = configuration.getValueOrDefault(urlParameter, url)
    ids = configuration.getValueOrDefault(idsParameter, ids)
    limit = configuration.getValueOrDefault(limitParameter, limit)
    earliest = configuration.getValueOrDefault(earliestParameter, earliest)
    latest = configuration.getValueOrDefault(latestParameter, latest)
  }

  private def tryPush(): Unit = {

    if (!isAvailable(out) || metricCollections.isEmpty) return

    val metric = metricCollections.head

    push(out, MetricConversion.convertMetricCollectionToDataset(metric._1, metric._2))

    metricCollections -= metric
  }

  private def scrapeMetrics(): Unit = {

    ids
      .map(_.trim)
      .map(id => {

      val newest = idToEarliest.getOrElse(id, earliest)
      val mq = MetricsQuery(limit, newest, latest, format)

      val uri = Uri(s"$url/metrics/$id")
      (id, HttpRequest(HttpMethods.POST, uri, entity = HttpEntity(ContentTypes.`application/json`, write(mq))), Promise[HttpResponse])

    })
      .foreach({
        case (id, request, promise) =>
          queue.offer((request, promise)).flatMap(_ => promise.future).onComplete({
            case Success(response) =>
              parseAsync.invoke(id, response)
            case Failure(cause) =>
              log.error(s"Couldn't retrieve metrics: $cause")
          })
      })
  }

  private def parseHttpResponse(response: HttpResponse): Seq[MetricsCollection] = {
    val body = response.entity.asInstanceOf[HttpEntity.Strict].data.utf8String

    org.json4s.native.Serialization.read[Seq[MetricsCollection]](body)
  }

  private def getEarliest(mcs: Seq[MetricsCollection]): String = {
    val timestampValue = MetricConversion.getLatest(mcs.head)
    MetricConversion.timestampToString(timestampValue, format)
  }
}

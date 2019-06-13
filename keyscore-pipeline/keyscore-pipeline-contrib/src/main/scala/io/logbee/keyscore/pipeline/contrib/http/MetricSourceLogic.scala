package io.logbee.keyscore.pipeline.contrib.http

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
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.{Formats, Serialization}

import scala.collection.mutable
import scala.util.{Failure, Success}

object MetricSourceLogic extends Described {

  val serverParameter = TextParameterDescriptor(
    ref = "metric.source.server",
    info = ParameterInfo(
      displayName = TextRef("bootstrapServer"),
      description = TextRef("serverDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "localhost",
    mandatory = true
  )

  val portParameter = NumberParameterDescriptor(
    "metric.source.port",
    ParameterInfo(
      TextRef("port"),
      TextRef("portDescription")),
    defaultValue = 4711L,
    range = NumberRange(step = 1, end = 65535),
    mandatory = true
  )

  val intervalParameter = NumberParameterDescriptor(
    "metric.source.interval",
    ParameterInfo(
      TextRef("interval"),
      TextRef("intervalDescription")),
    defaultValue = 5L,
    range = NumberRange(step = 1, end = Long.MaxValue),
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

  val formatParameter = TextParameterDescriptor(
    ref = "metric.source.format",
    info = ParameterInfo(
      displayName = TextRef("format"),
      description = TextRef("formatDescription")
    ),
    defaultValue = "dd.MM.yyy_HH:mm:ss:nnnnnnnnn",
    mandatory = true
  )

  val earliestParameter = TextParameterDescriptor(
    ref = "metric.source.earliest",
    info = ParameterInfo(
      displayName = TextRef("earliest"),
      description = TextRef("earliestDescription")
    ),
    defaultValue = "01.01.2000_00:00:00:000000000",
    mandatory = true
  )

  val latestParameter = TextParameterDescriptor(
    ref = "metric.source.latest",
    info = ParameterInfo(
      displayName = TextRef("latest"),
      description = TextRef("latestDescription")
    ),
    defaultValue = "31.12.9999_23:59:59:999999999",
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "6a9671d9-93a9-4fe4-b779-b4e0cf9a6e6c",
    describes = SourceDescriptor(
      name = classOf[MetricSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SOURCE, Category("HTTP"), Category("Metric")),
      parameters = Seq(
        serverParameter,
        portParameter,
        intervalParameter,
        idsParameter,
        limitParameter,
        formatParameter,
        earliestParameter,
        latestParameter
      )
      //      ,
      //      icon = Icon.fromClass(classOf[MetricSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.http.MetricSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class MetricSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {

  import MetricSourceLogic._

  implicit private val formats: Formats = KeyscoreFormats.formats
  implicit val serialization: Serialization = Serialization

  private var server = "http://localhost"
  private var port: Long = 4711
  private var interval: Long = 5
  private var ids = Seq.empty[String]
  private var limit = Long.MaxValue
  private var format = "dd.MM.yyy_HH:mm:ss:nnnnnnnnn"
  private var earliest = "01.01.2000_00:00:00:000000000"
  private var latest = "31.12.9999_23:59:59:999999999"

  private val metricCollections = mutable.HashMap.empty[String, Seq[MetricsCollection]]
  private val idToEarliest = mutable.HashMap.empty[String, String]

  private val parseAsync = getAsyncCallback[(String, HttpResponse)]({case (id, response) =>
    metricCollections += (id -> parseHttpResponse(response))
  })

  override def initialize(configuration: Configuration): Unit = {
    //1. Configure
    configure(configuration)

    //2. Send first HTTP Request and store processed responses in the HashMap
    collectMetrics()
  }

  override def configure(configuration: Configuration): Unit = {
    applyConfiguration(configuration)

  }

  override def onPull(): Unit = {

    pushMetrics()

    //3. Clear the HashMap
    metricCollections.clear()

    //4. Wait _interval_ and _collectMetrics()_ with


  }

  private def applyConfiguration(configuration: Configuration): Unit = {
    server = configuration.getValueOrDefault(serverParameter, server)
    port = configuration.getValueOrDefault(portParameter, port)
    interval = configuration.getValueOrDefault(intervalParameter, interval)
    ids = configuration.getValueOrDefault(idsParameter, ids)
    limit = configuration.getValueOrDefault(limitParameter, limit)
    format = configuration.getValueOrDefault(formatParameter, format)
    earliest = configuration.getValueOrDefault(earliestParameter, earliest)
    latest = configuration.getValueOrDefault(latestParameter, latest)
  }

  private def collectMetrics(): Unit = {

    ids.map(id => {

      val newest = idToEarliest.getOrElse(id, earliest)
      val mq = MetricsQuery(limit, newest, latest, format)

      //1.2 Send n HTTP Request to get the MetricCollections for the _id's_
      val uri = Uri(s"$server:$port/metrics/$id")
      id -> Http().singleRequest(HttpRequest(HttpMethods.POST, uri, entity = HttpEntity(ContentTypes.`application/json`, write(mq))))

    })
      .foreach({
        case (id, future) =>
          future.onComplete({
            case Success(response) =>
              parseAsync.invoke((id, response))
            case Failure(cause) =>
              log.error(s"Retrieve MetricCollections for <$id> failed: $cause")
          })
      })
  }

  private def parseHttpResponse(response: HttpResponse): Seq[MetricsCollection] = {
    val body = response.entity.asInstanceOf[HttpEntity.Strict].data.utf8String

    org.json4s.native.Serialization.read[Seq[MetricsCollection]](body)
  }

  private def pushMetrics(): Unit = {
    metricCollections.foreach(mcs => {



      mcs._2.foreach(mc => {
        push(out, MetricConversion.convertMetricCollectionToDataset(mcs._1, mc))
      })
    })
  }






}

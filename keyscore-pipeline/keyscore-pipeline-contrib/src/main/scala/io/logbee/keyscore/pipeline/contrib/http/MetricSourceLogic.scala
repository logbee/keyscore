package io.logbee.keyscore.pipeline.contrib.http

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.MediaTypes.`application/json`
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
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.{Formats, Serialization}

import scala.collection.mutable
import scala.concurrent.Promise
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

  // TODO: Replace with DateTimeParameterDescriptor...when we have one. ;-)
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

  private var server = "localhost"
  private var port = 4711L
  private var interval = 5L
  private var ids = Seq.empty[String]
  private var limit = Long.MaxValue
  private var format = "dd.MM.yyy_HH:mm:ss:nnnnnnnnn"
  private var earliest = "01.01.2000_00:00:00:000000000"
  private var latest = "31.12.9999_23:59:59:999999999"

  private var queue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] = _

  private val metricCollections = mutable.HashMap.empty[String, Seq[MetricsCollection]]
  private val idToEarliest = mutable.HashMap.empty[String, String]

  private val parseAsync = getAsyncCallback[HttpResponse](response => parseHttpResponse(response))

  override def initialize(configuration: Configuration): Unit = {
    //1. Configure
    configure(configuration)

    //2. Send first HTTP Request and store processed responses in the HashMap
    collectMetrics()
  }

  override def configure(configuration: Configuration): Unit = {
    applyConfiguration(configuration)

    val pool = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = server, port = port.intValue())
    queue = Source.queue[(HttpRequest, Promise[HttpResponse])](1, OverflowStrategy.dropNew)
      .via(pool)
      .toMat(Sink.foreach({
        case (Success(response), promise) => promise.success(response)
        case (Failure(throwable), promise) => promise.failure(throwable)
      }))(Keep.left)
      .run()

  }

  override def onPull(): Unit = {

    //1. Convert MetricCollections in the HashMap into datasets and push them

    //2. _updateEarliest()_

    //3. Clear the HashMap
    metricCollections.clear()

    //4. Wait _interval_ and _collectMetrics()_ with


  }

  private def collectMetrics(): Unit = {


    ids.map(id => {
      //TODO Update earliest for each id
      //1.1 Create a MetricsQuery
      val mq = MetricsQuery(limit, earliest, latest, format)

      //1.2 Send n HTTP Request to get the MetricCollections for the _id's_
      HttpRequest(POST, uri = s"/metrics/$id", entity = HttpEntity(`application/json`, write(mq))) -> Promise[HttpResponse]

    })
      .foreach({
        case tuple@(request, promise) =>
          queue.offer(tuple).flatMap(_ => promise.future).onComplete({
            case Success(response) =>
              //2. Parse the HTTP responses to Seq[MetricCollection] and store them in the HashMap
              parseAsync.invoke(response)
            case Failure(cause) => log.error(s"Retrieve MetricCollections for ${request.uri.path} failed: $cause")
          })
      })
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

  private def parseHttpResponse(response: HttpResponse): Unit = {

    org.json4s.native.Serialization.read[Seq[MetricsCollection]](response.entity.toString)

//    implicit val um: Unmarshaller[HttpEntity, Seq[MetricsCollection]] = {
//      Unmarshaller.byteStringUnmarshaller.mapWithCharset { (data, charset) =>
//        val charBuffer = Unmarshaller.bestUnmarshallingCharsetFor(data)
////        JsonFormat.fromJsonString(data.decodeString(charBuffer.nioCharset().name()))(MetricsCollection)
//        /*PropertyEntity.parseFrom(CodedInputStream.newInstance(data.asByteBuffer))*/
//
//      }
//    }
//
//    val mcs = Unmarshal(response.entity)
  }

  private def convertMetricCollections(mcs: Seq[MetricsCollection]): Seq[Dataset] = {
    //TODO implement
    val datasets: Seq[Dataset] = mcs.map(mc => {
      Dataset()
    })

    datasets
  }


}

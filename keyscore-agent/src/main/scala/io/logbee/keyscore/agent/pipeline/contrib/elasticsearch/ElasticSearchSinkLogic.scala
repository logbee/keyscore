package io.logbee.keyscore.agent.pipeline.contrib.elasticsearch

import java.util.{Locale, ResourceBundle, UUID}

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, SinkShape}
import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.agent.pipeline.stage.{SinkLogic, StageContext}
import io.logbee.keyscore.commons.util.Hashing._
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import org.json4s.NoTypeHints
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

import scala.collection.mutable
import scala.concurrent.Promise
import scala.language.postfixOps
import scala.util.{Failure, Success}

object ElasticSearchSinkLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.elasticsearch.ElasticSearchSinkLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.ElasticSearchSinkLogic"
  private val filterId = "6693c39e-6261-11e8-adc0-fa7ae01bbebc"

  val descriptorMap = mutable.Map.empty[Locale, FilterDescriptorFragment]


  override def describe: MetaFilterDescriptor = {
    val fragments = Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(UUID.fromString(filterId), filterName, fragments)
  }

  private def descriptor(language: Locale) = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(bundleName, language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = false),
      parameters = List(
        TextParameterDescriptor("host", translatedText.getString("host"), "description"),
        IntParameterDescriptor("port", translatedText.getString("port"), "description"),
        TextParameterDescriptor("index", translatedText.getString("index"), "description")
      ), translatedText.getString("category"))
  }

  private case class Document(_id: String, fields: Map[String, _])
}

class ElasticSearchSinkLogic(context: StageContext, configuration: FilterConfiguration, shape: SinkShape[Dataset]) extends SinkLogic(context, configuration, shape) {

  private var elasticHost: String = _
  private var elasticPort: Int = _
  private var elasticIndex: String = _

  private var queue:  SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] = _

  private val pullAsync = getAsyncCallback[Unit](_ => pull(in))

  private implicit val formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all

  override def initialize(configuration: FilterConfiguration): Unit = {

    elasticHost = configuration.getParameterValue[String]("host")
    elasticPort = configuration.getParameterValue[Int]("port")
    elasticIndex = configuration.getParameterValue[String]("index")

    configure(configuration)

    pull(in)
  }

  override def configure(configuration: FilterConfiguration): Unit = {

    val pool = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = elasticHost, port = elasticPort)
    queue = Source.queue[(HttpRequest, Promise[HttpResponse])](1, OverflowStrategy.dropNew)
      .via(pool)
      .toMat(Sink.foreach({
        case (Success(response), promise) => promise.success(response)
        case (Failure(throwable), promise) => promise.failure(throwable)
      }))(Keep.left)
      .run()
  }

  override def onPush(): Unit = {
    grab(in).records
      .map(record => {
        val fields = record.fields.foldLeft(Map.empty[String, Any])({
          case (map, Field(name, value)) =>
            value match {
              case TextValue(text) => map + (name -> text)
              case NumberValue(number) => map + (name -> number)
              case DecimalValue(decimal) => map + (name -> decimal)
              case timestampValue: TimestampValue => map + (name -> Timestamps.toString(timestampValue))
              case _ => map
            }
        })

        HttpRequest(POST, uri = s"/$elasticIndex/_doc/${fields.hashCode().base64()}/", entity = HttpEntity(`application/json`, write(fields))) -> Promise[HttpResponse]
      })
      .foreach({
        case tuple @ (request, promise) =>
          queue.offer(tuple).flatMap(_ => promise.future).onComplete({
            case Success(response) =>
              log.info(s"$response")
              pullAsync.invoke()
            case Failure(cause) => log.error(s"Send datasets to elastic failed: $cause")
          })
      })
  }
}

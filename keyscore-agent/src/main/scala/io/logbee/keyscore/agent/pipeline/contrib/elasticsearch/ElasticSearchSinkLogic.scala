package io.logbee.keyscore.agent.pipeline.contrib.elasticsearch

import java.util.{Locale, ResourceBundle, UUID}

import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, SinkShape}
import io.logbee.keyscore.agent.pipeline.stage.{SinkLogic, StageContext}
import io.logbee.keyscore.commons.util.Hashing._
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described}
import org.json4s.NoTypeHints
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import java.util.UUID.fromString

import akka.http.scaladsl.Http
import io.logbee.keyscore.agent.pipeline.contrib.kafka.KafkaSinkLogic.{filterId, filterName}

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
        TextParameterDescriptor(translatedText.getString("host")),
        TextParameterDescriptor(translatedText.getString("port")),
        TextParameterDescriptor(translatedText.getString("index"))
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

    val pool = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = "localhost", port = 9200)
    queue = Source.queue[(HttpRequest, Promise[HttpResponse])](1, OverflowStrategy.dropNew)
      .via(pool)
      .toMat(Sink.foreach({
        case ((Success(response), promise)) => promise.success(response)
        case ((Failure(throwable), promise)) => promise.failure(throwable)
      }))(Keep.left)
      .run()
  }

  override def onPush(): Unit = {

    grab(in)
      .map(record => {
        val fields = record.payload.values.foldLeft(Map.empty[String, Any])({
          case (map, field) => map + (field.name -> field.value)
        })

        HttpRequest(POST, uri = s"/$elasticIndex/_doc/${fields.hashCode().base64()}/", entity = HttpEntity(`application/json`, write(fields))) -> Promise[HttpResponse]
      })
      .foreach({
        case tuple @ (request, promise) =>
          queue.offer(tuple).flatMap(_ => promise.future).onComplete({
            case Success(response) =>
              log.info(s"$response")
              pullAsync.invoke()
            case _ =>
          })
      })
  }
}

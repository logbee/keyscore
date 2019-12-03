package io.logbee.keyscore.pipeline.contrib.elasticsearch

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, SinkShape}
import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.Hashing._
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.elasticsearch.ElasticSearchSinkLogic.{hostParameter, indexParameter, portParameter}
import org.json4s.{Formats, NoTypeHints}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

import scala.concurrent.Promise
import scala.util.{Failure, Success}

object ElasticSearchSinkLogic extends Described {

  val hostParameter = TextParameterDescriptor(
    "elastic.host",
    ParameterInfo(
      displayName = TextRef("elastic.host.displayName"),
      description = TextRef("elastic.host.description")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "example.com",
    mandatory = true
  )

  val portParameter = NumberParameterDescriptor(
    "elastic.port",
    ParameterInfo(
      displayName = TextRef("elastic.port.displayName"),
      description = TextRef("elastic.port.description")
    ),
    defaultValue = 9200,
    range = NumberRange(1, 0, 65535),
    mandatory = true
  )

  val indexParameter = TextParameterDescriptor(
    "elastic.index",
    ParameterInfo(
      displayName = TextRef("elastic.index.displayName"),
      description = TextRef("elastic.index.description")
    ),
    defaultValue = "doc",
    mandatory = true
  )

  val authenticationRequiredParameter = BooleanParameterDescriptor(
    ref = "elastic.authentication.required",
    ParameterInfo(
      displayName = TextRef("elastic.authentication.required.displayName"),
      description = TextRef("elastic.authentication.required.description")
    ),
    defaultValue = false,
  )

  val authenticationUserParameter = TextParameterDescriptor(
    ref = "elastic.authentication.user",
    ParameterInfo(
      displayName = TextRef("elastic.authentication.username.displayName"),
      description = TextRef("elastic.authentication.username.description")
    ),
  )

  val authenticationPasswordParameter = PasswordParameterDescriptor(
    ref = "elastic.authentication.password",
    ParameterInfo(
      displayName = TextRef("elastic.authentication.password.displayName"),
      description = TextRef("elastic.authentication.password.description")
    ),
    maxLength = Int.MaxValue
  )

  override def describe = Descriptor(
    ref = "6693c39e-6261-11e8-adc0-fa7ae01bbebc",
    describes = SinkDescriptor(
      name = classOf[ElasticSearchSinkLogic].getName,
      displayName = TextRef("elastic.displayName"),
      description = TextRef("elastic.description"),
      categories = Seq(CommonCategories.SINK, Category("Elasticsearch")),
      parameters = Seq(
        hostParameter,
        portParameter,
        indexParameter,
        authenticationRequiredParameter,
        authenticationUserParameter,
        authenticationPasswordParameter
      ),
      icon = Icon.fromClass(classOf[ElasticSearchSinkLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.elasticsearch.ElasticSearchSinkLogic",
      Locale.ENGLISH, Locale.GERMAN) ++ CATEGORY_LOCALIZATION
  )

  private case class Document(_id: String, fields: Map[String, _])
}

class ElasticSearchSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape) {

  private var elasticHost: String = hostParameter.defaultValue
  private var elasticPort: Int = portParameter.defaultValue.asInstanceOf[Int]
  private var elasticIndex: String = indexParameter.defaultValue

  private var queue:  SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] = _

  private val pullAsync = getAsyncCallback[Unit](_ => pull(in))

  private implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all

  override def initialize(configuration: Configuration): Unit = {

    elasticHost = configuration.getValueOrDefault(hostParameter, elasticHost)
    elasticPort = configuration.getValueOrDefault(portParameter, elasticPort).asInstanceOf[Int]
    elasticIndex = configuration.getValueOrDefault(indexParameter, elasticIndex)

    configure(configuration)

    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {

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
              case TextValue(text, _) => map + (name -> text)
              case NumberValue(number) => map + (name -> number)
              case DecimalValue(decimal) => map + (name -> decimal)
              case timestampValue: TimestampValue => map + (name -> Timestamps.toString(timestampValue))
              case _ => map
            }
        })

        HttpRequest(POST, uri = s"/$elasticIndex/_doc/${fields.hashCode().base64}/", entity = HttpEntity(`application/json`, write(fields))) -> Promise[HttpResponse]
      })
      .foreach({
        case tuple @ (_, promise) =>
          queue.offer(tuple).flatMap(_ => promise.future).onComplete({
            case Success(response) =>
              log.info(s"$response")
              pullAsync.invoke(())
            case Failure(cause) => log.error(s"Send datasets to elastic failed: $cause")
          })
      })
  }
}

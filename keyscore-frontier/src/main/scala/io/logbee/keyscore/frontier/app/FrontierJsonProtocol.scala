package io.logbee.keyscore.frontier.app

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.logbee.keyscore.frontier.filters.GrokFilterConfiguration
import io.logbee.keyscore.frontier.filters.GrokFilterConfiguration.GrokFilterConfigurationApply
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.sink.{KafkaSinkModel, SinkModel, SinkTypes}
import io.logbee.keyscore.model.source.{KafkaSourceModel, SourceModel, SourceTypes}
import spray.json.{DefaultJsonProtocol, RootJsonFormat, _}

trait FrontierJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val streamFormat = jsonFormat4(StreamModel)
  implicit val kafkaSourceFormat = jsonFormat5(KafkaSourceModel)
  implicit val kafkaSinkFormat = jsonFormat3(KafkaSinkModel)
  implicit val extractFieldsFilterFormat = jsonFormat3(RetainFieldsFilterModel)
  implicit val addFieldsFilterFormat = jsonFormat3(AddFieldsFilterModel)
  implicit val removeFieldsFilterFormat = jsonFormat3(RemoveFieldsFilterModel)
  implicit val grokFilterFormat = jsonFormat5(GrokFilterModel)
  implicit val grokFilterConfiguration = jsonFormat3(GrokFilterConfigurationApply)
  implicit object SourceJsonFormat extends RootJsonFormat[SourceModel] {
    def write(source: SourceModel) = source match {
      case kafka: KafkaSourceModel => kafka.toJson
    }

    def read(value: JsValue) =
      value.asJsObject.fields(SourceTypes.SourceType) match {
        case JsString(SourceTypes.KafkaSource) => value.convertTo[KafkaSourceModel]
      }
  }

  implicit object SinkJsonFormat extends RootJsonFormat[SinkModel] {
    def write(sink: SinkModel) = sink match {
      case kafka: KafkaSinkModel => kafka.toJson
    }

    def read(value: JsValue) =
      value.asJsObject.fields(SinkTypes.SinkType) match {
        case JsString(SinkTypes.KafkaSink) => value.convertTo[KafkaSinkModel]
      }
  }

  implicit object FilerJsonFormat extends RootJsonFormat[FilterModel] {
    def write(filter: FilterModel) = filter match {
      case extract: RetainFieldsFilterModel => extract.toJson
      case add: AddFieldsFilterModel => add.toJson
      case remove: RemoveFieldsFilterModel => remove.toJson
      case grok: GrokFilterModel => grok.toJson
    }

    def read(value: JsValue) =
      value.asJsObject.fields(FilterTypes.FilterType) match {
        case JsString(FilterTypes.RetainFields) => value.convertTo[RetainFieldsFilterModel]
        case JsString(FilterTypes.AddFields) => value.convertTo[AddFieldsFilterModel]
        case JsString(FilterTypes.RemoveFields) => value.convertTo[RemoveFieldsFilterModel]
        case JsString(FilterTypes.GrokFields) => value.convertTo[GrokFilterModel]
      }
  }

  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _              => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }


}

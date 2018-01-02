package io.logbee.keyscore.frontier.app

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.sink.{KafkaSinkModel, SinkModel, SinkTypes}
import io.logbee.keyscore.model.source.{KafkaSourceModel, SourceModel, SourceTypes}
import spray.json.{DefaultJsonProtocol, RootJsonFormat, _}

trait FrontierJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val streamFormat = jsonFormat4(StreamModel)
  implicit val kafkaSourceFormat = jsonFormat5(KafkaSourceModel)
  implicit val kafkaSinkFormat = jsonFormat3(KafkaSinkModel)
  implicit val extractFieldsFilterFormat = jsonFormat2(ExtractFieldsFilterModel)
  implicit val addFieldsFilterFormat = jsonFormat2(AddFieldsFilterModel)
  implicit val removeFieldsFilterFormat = jsonFormat2(RemoveFieldsFilterModel)
  implicit val extractToNewFieldFilterFormat = jsonFormat5(ExtractToNewFieldFilterModel)

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
      case extract: ExtractFieldsFilterModel => extract.toJson
      case add: AddFieldsFilterModel => add.toJson
      case remove: RemoveFieldsFilterModel => remove.toJson
      case extractTo: ExtractToNewFieldFilterModel => extractTo.toJson
    }

    def read(value: JsValue) =
      value.asJsObject.fields(FilterTypes.FilterType) match {
        case JsString(FilterTypes.ExtractFields) => value.convertTo[ExtractFieldsFilterModel]
        case JsString(FilterTypes.AddFields) => value.convertTo[AddFieldsFilterModel]
        case JsString(FilterTypes.RemoveFields) => value.convertTo[RemoveFieldsFilterModel]
        case JsString(FilterTypes.ExtractToNew) => value.convertTo[ExtractToNewFieldFilterModel]
      }
  }


}

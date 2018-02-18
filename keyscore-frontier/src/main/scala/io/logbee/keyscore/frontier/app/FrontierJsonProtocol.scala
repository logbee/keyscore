package io.logbee.keyscore.frontier.app

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.logbee.keyscore.frontier.cluster.RemoteAgent
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
  implicit val appInfoFormat = jsonFormat3(AppInfo.apply)
  implicit val remoteAgentFormat = jsonFormat3(RemoteAgent)

  implicit val filterDescriptor = jsonFormat4(FilterDescriptor.apply)


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
        case _ => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

  implicit object ParameterDescriptorFormat extends RootJsonFormat[ParameterDescriptor] {
    override def write(obj: ParameterDescriptor): JsValue = obj match {
      case boolean: BooleanParameterDescriptor => boolean.toJson
      case text: TextParameterDescriptor => text.toJson
      case list: ListParameterDescriptor => list.toJson
      case map: MapParameterDescriptor => map.toJson
    }

    override def read(jsValue: JsValue) = {
      jsValue.asJsObject().fields("kind") match {
        case JsString("boolean") => jsValue.convertTo[BooleanParameterDescriptor]
        case JsString("text") => jsValue.convertTo[TextParameterDescriptor]
        case JsString("list") => jsValue.convertTo[ListParameterDescriptor]
        case JsString("map") => jsValue.convertTo[MapParameterDescriptor]
      }
    }
  }

  implicit object BooleanParameterJsonFormat extends RootJsonFormat[BooleanParameterDescriptor] {

    case class booleanDummy(name: String, displayName: String, mandatory: Boolean, kind: String)
    implicit val booleanDummyForamt = jsonFormat4(booleanDummy)

    def write(booleanParameter: BooleanParameterDescriptor) = {
      val formatDummy = booleanDummy(booleanParameter.name, booleanParameter.displayName, booleanParameter.mandatory, booleanParameter.kind)
      formatDummy.toJson
    }

    def read(value: JsValue) = {
      val formatBoolean = value.convertTo[booleanDummy]
      BooleanParameterDescriptor.apply(formatBoolean.name)
    }
  }

  implicit object TextParameterJsonFormat extends RootJsonFormat[TextParameterDescriptor] {

    case class textDummy(name: String, displayName: String, mandatory: Boolean, validator: String, kind: String)
    implicit val textDummyFormat = jsonFormat5(textDummy)

    def write(textParameter: TextParameterDescriptor) = {
      val formatDummy = textDummy(textParameter.name, textParameter.displayName, textParameter.mandatory, textParameter.validator, textParameter.kind)
      formatDummy.toJson
    }

    def read(value: JsValue) = {
      val formatText = value.convertTo[textDummy]
      TextParameterDescriptor.apply(formatText.name, formatText.displayName, formatText.mandatory, formatText.validator)
    }
  }

  implicit object ListParameterJsonFormat extends RootJsonFormat[ListParameterDescriptor] {

    case class listDummy(name: String, displayName: String, mandatory: Boolean, element: ParameterDescriptor, min: Int, max: Int, kind: String)
    implicit val listDummyFormat = jsonFormat7(listDummy)

    def write(listParameter: ListParameterDescriptor) = {
      val formatDummy = listDummy(listParameter.name, listParameter.displayName, listParameter.mandatory, listParameter.element, listParameter.min, listParameter.max, listParameter.kind)
      formatDummy.toJson
    }

    def read(value: JsValue) = {
      val formatList = value.convertTo[listDummy]
      ListParameterDescriptor.apply(formatList.name, formatList.element, formatList.min, formatList.max)
    }
  }


  implicit object MapParameterJsonFormat extends RootJsonFormat[MapParameterDescriptor] {

    case class mapDummy(name: String, displayName: String, mandatory: Boolean, key: ParameterDescriptor, value: ParameterDescriptor, min: Int, max: Int, kind: String)
    implicit val mapDummyFormat = jsonFormat8(mapDummy)

    def write(mapParameter: MapParameterDescriptor) = {
      val formatDummy = mapDummy(mapParameter.name, mapParameter.displayName, mapParameter.mandatory, mapParameter.key, mapParameter.value, mapParameter.min, mapParameter.max, mapParameter.kind)
      formatDummy.toJson
    }

    def read(value: JsValue) = {
      val formatMap = value.convertTo[mapDummy]
      MapParameterDescriptor.apply(formatMap.name, formatMap.key, formatMap.value, formatMap.min, formatMap.max)
    }
  }


}

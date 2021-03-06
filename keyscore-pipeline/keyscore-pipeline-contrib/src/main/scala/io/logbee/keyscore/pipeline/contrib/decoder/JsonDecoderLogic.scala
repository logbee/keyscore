package io.logbee.keyscore.pipeline.contrib.decoder

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories.{CATEGORY_LOCALIZATION, DECODING, JSON}
import org.json4s.JsonAST._
import org.json4s.native.JsonParser._

object JsonDecoderLogic extends Described {

  val sourceFieldNameParameter = FieldNameParameterDescriptor(
    ref = "sourceFieldName",
    info = ParameterInfo(
      displayName = TextRef("sourceFieldName.displayName"),
      description = TextRef("sourceFieldName.description")
    ),
    defaultValue = "message",
    hint = PresentField,
    mandatory = true
  )

  val removeSourceFieldParameter = BooleanParameterDescriptor(
    ref = "removeSourceField",
    info = ParameterInfo(
      displayName = TextRef("removeSourceField.displayName"),
      description = TextRef("removeSourceField.description")
    ),
    defaultValue = false,
    mandatory = true
  )

  override def describe = Descriptor (
    ref = "56c8a366-8625-46d2-9c0f-92dd24519989",
    describes = FilterDescriptor(
      name = classOf[JsonDecoderLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(DECODING, JSON),
      parameters = Seq(sourceFieldNameParameter, removeSourceFieldParameter),
      icon = Icon.fromClass(classOf[JsonDecoderLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.decoder.JsonDecoder",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class JsonDecoderLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var sourceFieldName = JsonDecoderLogic.sourceFieldNameParameter.defaultValue
  private var removeSourceField = JsonDecoderLogic.removeSourceFieldParameter.defaultValue

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    sourceFieldName = configuration.getValueOrDefault(JsonDecoderLogic.sourceFieldNameParameter, sourceFieldName)
    removeSourceField = configuration.getValueOrDefault(JsonDecoderLogic.removeSourceFieldParameter, removeSourceField)
  }

  override def onPush(): Unit = {
    val dataset = grab(in)

    val records = dataset.records.map(record => {

      val sourceField = record.fields.find(sourceFieldName == _.name)

      if (sourceField.isDefined && sourceField.get.isTextField) {

        val root = parse(sourceField.get.toTextField.value)
        val fields = extract(root, List.empty, List.empty)

        if (removeSourceField) {
          Record(record.fields.filter(sourceFieldName != _.name ) ++ fields)
        }
        else {
          Record(record.fields ++ fields)
        }
      }
      else {
        record
      }
    })

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def extract(node: JValue, path: List[String], fields: List[Field] = List.empty): List[Field] = {
    node match {
      case obj: JObject =>
        obj.obj.foldLeft(fields) {
          case (fields, (name, jValue)) =>
            extract(jValue, path :+ name, fields)
        }

      case JArray(elements) =>
        elements.zipWithIndex.foldLeft(fields) {
          case (fields, (jValue, index)) =>
            extract(jValue, path :+ index.toString, fields)
        }

      case JBool(value) =>
        fields :+ Field(path.mkString("."), BooleanValue(value))

      case JInt(value) =>
        fields :+ Field(path.mkString("."), NumberValue(value.toLong))

      case JLong(value) =>
        fields :+ Field(path.mkString("."), NumberValue(value.toLong))

      case JDouble(value) =>
        fields :+ Field(path.mkString("."), DecimalValue(value))

      case JDecimal(value) =>
        fields :+ Field(path.mkString("."), DecimalValue(value.toDouble))

      case JString(value) =>
        fields :+ Field(path.mkString("."), TextValue(value))

      case _ =>
        fields
    }
  }
}

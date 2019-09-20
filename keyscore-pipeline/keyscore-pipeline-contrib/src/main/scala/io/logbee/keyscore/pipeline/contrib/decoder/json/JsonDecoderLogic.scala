package io.logbee.keyscore.pipeline.contrib.decoder.json

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
import io.logbee.keyscore.pipeline.commons.CommonCategories.{CATEGORY_LOCALIZATION, DECODING, JSON}
import org.json4s.JsonAST._
import org.json4s.native.JsonParser._

import scala.collection.mutable

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
      bundleName = "io.logbee.keyscore.pipeline.contrib.decoder.json.JsonDecoder",
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

    push(out, dataset.update(_.records := dataset.records.foldLeft(mutable.ListBuffer.empty[Record]) {

      case (result, record) =>

        val sourceField = record.fields.find(sourceFieldName == _.name)

        if (sourceField.isDefined && sourceField.get.isTextField) {
          try {
            result ++= (parse(sourceField.get.toTextField.value) match {
              case obj @ JObject(_) => List(Record(JsonDecoderUtil.extract(obj, List.empty, List.empty)))
              case JArray(arr) => arr.map(obj => Record(JsonDecoderUtil.extract(obj, List.empty, List.empty)))
              case _ => List.empty
            })
  
            if (removeSourceField) {
  
              val remainingFields = record.fields.filter(sourceFieldName != _.name )
  
              if (remainingFields.nonEmpty) {
  
                result += Record(remainingFields)
              }
            }
            else {
              result += record
            }
          }
          catch {
            case ex: Throwable =>
              log.error(ex, "Failed to decode.")
          }
        }

        result

    }.toList))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

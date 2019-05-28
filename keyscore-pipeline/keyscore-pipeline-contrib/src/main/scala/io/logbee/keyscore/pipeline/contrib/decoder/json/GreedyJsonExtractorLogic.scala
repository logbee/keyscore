package io.logbee.keyscore.pipeline.contrib.decoder.json

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Icon, Record}
import io.logbee.keyscore.model.descriptor.Maturity.Experimental
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories._

object GreedyJsonExtractorLogic extends Described {

  val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "fieldname",
    info = ParameterInfo(
      displayName = "fieldName.displayName",
      description = "fieldName.description"
    ),
    mandatory = true
  )

  val prefixParameter = TextParameterDescriptor(
    ref = "prefix",
    info = ParameterInfo(
      displayName = "prefix.displayName",
      description = "prefix.description"
    ),
    mandatory = false,
    defaultValue = "greedy"
  )

  val removeFieldParameter = BooleanParameterDescriptor(
    ref = "removeField",
    info = ParameterInfo(
      displayName = TextRef("removeField.displayName"),
      description = TextRef("removeField.description")
    ),
    defaultValue = false,
    mandatory = false
  )

  override def describe = Descriptor(
    ref = "7ffff5e5-4565-4cc5-85f4-ad1bb73bdd36",
    describes = FilterDescriptor(
      name = classOf[GreedyJsonExtractorLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(DECODING, JSON),
      parameters = Seq(fieldNameParameter, prefixParameter, removeFieldParameter),
      maturity = Experimental,
      icon = Icon.fromClass(classOf[GreedyJsonExtractorLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.decoder.json.GreedyJsonExtractor",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class GreedyJsonExtractorLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var prefix = GreedyJsonExtractorLogic.prefixParameter.defaultValue
  private var fieldName = GreedyJsonExtractorLogic.fieldNameParameter.defaultValue
  private var removeField = GreedyJsonExtractorLogic.removeFieldParameter.defaultValue

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    fieldName = configuration.getValueOrDefault(GreedyJsonExtractorLogic.fieldNameParameter, fieldName)
    prefix = configuration.getValueOrDefault(GreedyJsonExtractorLogic.prefixParameter, prefix)
    prefix = if (prefix.nonEmpty) prefix else GreedyJsonExtractorLogic.prefixParameter.defaultValue
    removeField = configuration.getValueOrDefault(GreedyJsonExtractorLogic.removeFieldParameter, removeField)
  }

  override def onPull(): Unit = {
    pull(in)
  }

  override def onPush(): Unit = {
    val dataset = grab(in)
    val records = dataset.records.map(record => {

      val sourceField = record.fields.find(fieldName == _.name)

      if (sourceField.isDefined && sourceField.get.isTextField) {

        val fields = JsonDecoderUtil.extract(sourceField.get.toTextField.value, prefix)

        if(removeField) {
          Record(record.fields.filter(_.name != fieldName) ++ fields)
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
}
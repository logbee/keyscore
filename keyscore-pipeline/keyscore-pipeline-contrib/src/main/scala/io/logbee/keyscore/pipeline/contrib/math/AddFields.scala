package io.logbee.keyscore.pipeline.contrib.math

import akka.stream.FlowShape
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION

object AddFields extends Described {

  val fieldListParameter = FieldNameListParameterDescriptor(
    ParameterRef("fieldList"),
    ParameterInfo(
      displayName = TextRef("fieldnames.DisplayName"),
      description = TextRef("fieldnames.Description")
    ),
    FieldNameParameterDescriptor("fieldList.item",
      ParameterInfo(
        displayName = TextRef("fieldnames.DisplayName"),
        description = TextRef("fieldnames.Description")
      ), "", FieldNameHint.AbsentField)
  )

  val targetFieldParameter = FieldNameParameterDescriptor(
    ParameterRef("targetField"),
    ParameterInfo(
      displayName = TextRef("targetField.DisplayName"),
      description = TextRef("targetField.Description")
    )
  )

  override def describe = Descriptor(
    ref = "8aa5c510-9bf0-11e9-9a38-8386b413b741",
    describes = FilterDescriptor(
      name = classOf[AddFields].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.MATH),
      parameters = Seq(fieldListParameter, targetFieldParameter),
      icon = Icon.fromClass(classOf[AddFields]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.math.AddFields",
      Locale.ENGLISH, Locale.GERMAN) ++ CATEGORY_LOCALIZATION
  )
}

class AddFields(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldsToAdd = Seq.empty[String]
  private var targetFieldName = "result"

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {

    fieldsToAdd = configuration.getValueOrDefault(AddFields.fieldListParameter, fieldsToAdd)
    targetFieldName = configuration.getValueOrDefault(AddFields.targetFieldParameter, targetFieldName)
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    if (fieldsToAdd.nonEmpty) {

      push(out, dataset.update(_.records := dataset.records.map(record => {
        record.update(_.fields :+= record.fields.foldLeft(0.0) {
          case (result, Field(name, NumberValue(value, _))) if fieldsToAdd.contains(name) => result + value
          case (result, Field(name, DecimalValue(value, _))) if fieldsToAdd.contains(name) => result + value
          case (result, _) => result
        }
          .map(result => Field(targetFieldName, DecimalValue(result)))
          .get)
      })))
    }
    else {
      push(out, dataset)
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

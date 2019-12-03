package io.logbee.keyscore.pipeline.contrib

import akka.stream.SourceShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Icon, MimeType, Record, TextValue}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.commons.CommonCategories

import scala.Int.MaxValue

object ConstantSourceLogic extends Described {

  val inputParameter = TextListParameterDescriptor(
    ParameterRef("input"),
    info = ParameterInfo(
      displayName = TextRef("input.displayName"),
      description = TextRef("input.description")
    ),
    descriptor = TextParameterDescriptor("input.item",ParameterInfo(
      displayName = TextRef("input.displayName"),
      description = TextRef("input.description")
    )),
    min = 1,
    max = MaxValue
  )

  val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "fieldName",
    info = ParameterInfo(
      displayName = TextRef("fieldName.displayName"),
      description = TextRef("fieldName.description")
    ),
    defaultValue = "message",
    hint = PresentField,
    mandatory = true
  )

  override def describe =  Descriptor(
    ref = "b789e90d-09c9-409f-9010-17cfb5c15019",
    describes = SourceDescriptor(
      name = classOf[ConstantSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.DEBUG, CommonCategories.SOURCE),
      parameters = Seq(inputParameter, fieldNameParameter),
      icon = Icon.fromClass(classOf[ConstantSourceLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.ConstantSourceLogic",
      Locale.ENGLISH, Locale.GERMAN) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class ConstantSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {

  private var inputData = Seq.empty[String]
  private var fieldName = ConstantSourceLogic.fieldNameParameter.defaultValue

  private var index = 0

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    inputData = configuration.getValueOrDefault(ConstantSourceLogic.inputParameter, inputData)
    fieldName = configuration.getValueOrDefault(ConstantSourceLogic.fieldNameParameter, fieldName)
  }

  override def onPull(): Unit = {

    push(out, Dataset(
      records = Record(
        fields = List(Field(
          fieldName,
          TextValue(inputData(index), Some(MimeType("text", "plain")))
        ))
      )
    ))

    index = if (index < inputData.size - 1) index + 1 else 0
  }
}

package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION

import scala.Int.MaxValue


object RemoveFieldsLogic extends Described {

  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RemoveFieldsFilter"
  private val iconName = "io.logbee.keyscore.pipeline.contrib.icon/remove.svg"

  val fieldsToRemoveParameter = FieldNameListParameterDescriptor(
    ref = "removeFields.fieldsToRemove",
    info = ParameterInfo(
      displayName = "fieldsToRemoveName",
      description = "fieldsToRemoveDescription"
    ),
    descriptor = FieldNameParameterDescriptor(
      hint = PresentField
    ),
    min = 1,
    max = MaxValue
  )

  override def describe = Descriptor(
    ref = "b7ee17ad-582f-494c-9f89-2c9da7b4e467",
    describes = FilterDescriptor(
      name = classOf[RemoveFieldsLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.REMOVE_DROP),
      parameters = Seq(fieldsToRemoveParameter),
      icon = Icon.fromClass(classOf[RemoveFieldsLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.RemoveFields",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class RemoveFieldsLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldsToRemove = Seq.empty[String]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    fieldsToRemove = configuration.getValueOrDefault(RemoveFieldsLogic.fieldsToRemoveParameter, fieldsToRemove)
  }

  override def onPush(): Unit = {
    val dataset = grab(in)
    val records = dataset.records.map(record => {
      Record(record.fields.filter(field => !fieldsToRemove.contains(field.name)))
    })

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

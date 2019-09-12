package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Icon, Record}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.filter.RenameFieldsLogic.fieldsToRenameParameter

object RenameFieldsLogic extends Described {

  val fieldsToRenameParameter = FieldListParameterDescriptor(
    ParameterRef("fieldsToRename"),
    ParameterInfo(
      displayName = TextRef("fieldsToRename"),
      description = TextRef("fieldsToRenameDescription")
    ),
    descriptor = FieldParameterDescriptor(
      //TODO: Pressent?
      hint = FieldNameHint.AbsentField
    )
  )

  override def describe = Descriptor(
      ref = "9b1428cc-6517-46cb-9e49-07b8569b1b9d",
      describes = FilterDescriptor(
        name = classOf[RenameFieldsLogic].getName,
        displayName = TextRef("displayName"),
        description = TextRef("description"),
        categories = Seq(CommonCategories.FIELDS),
        parameters = Seq(fieldsToRenameParameter),
        icon = Icon.fromClass(classOf[RenameFieldsLogic])
      ),
      localization = Localization.fromResourceBundle(
        bundleName = "io.logbee.keyscore.pipeline.contrib.filter.RenameFieldsLogic",
        Locale.ENGLISH, Locale.GERMAN) ++ CATEGORY_LOCALIZATION
    )
}

/**
  * Renames one or more fields. The old and new field names are passed in as key-value pairs.
  */
class RenameFieldsLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldsToRename = Seq.empty[Field]

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {

    fieldsToRename = configuration.getValueOrDefault(fieldsToRenameParameter, fieldsToRename)
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    val records = dataset.records.map(record => Record(fields = record.fields.map(field => {
      val fieldToRename = fieldsToRename.find(_.name == field.name)
      if (fieldToRename.nonEmpty)
        Field(fieldToRename.get.toTextField.value, field.value)
      else
        field
    })))

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

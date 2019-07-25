package io.logbee.keyscore.example.filter

import akka.stream.FlowShape
import io.logbee.keyscore.example.ExampleCategory
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Icon, Record}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}


object AddFieldsLogic extends Described {

  val fieldListParameter = FieldListParameterDescriptor(
    ParameterRef("fieldList"),
    ParameterInfo(
      displayName = TextRef("fieldsToAddName"),
      description = TextRef("fieldsToAddDescription")
    ),
    descriptor = FieldParameterDescriptor(
      hint = FieldNameHint.AbsentField
    )
  )

  override def describe = Descriptor(
      ref = "fb9ae22e-52c4-4b18-8fd1-d44ba6ff33e0",
      describes = FilterDescriptor(
        name = classOf[AddFieldsLogic].getName,
        displayName = TextRef("displayName"),
        description = TextRef("description"),
        categories = Seq(Category("example", TextRef("example.category.displayName"))),
        parameters = Seq(fieldListParameter),
        icon = Icon.fromClass(classOf[AddFieldsLogic])
      ),
      localization = Localization.fromResourceBundle(
        bundleName = "io.logbee.keyscore.example.filter.AddFieldsLogic",
        Locale.ENGLISH, Locale.GERMAN) ++ ExampleCategory.LOCALIZATION
    )
}

class AddFieldsLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldsToAdd = Seq.empty[Field]

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {

    fieldsToAdd = configuration.getValueOrDefault(AddFieldsLogic.fieldListParameter, fieldsToAdd)
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    if (fieldsToAdd.nonEmpty) {
      val records = dataset.records.map(record => Record(fields = record.fields ++ fieldsToAdd))
      push(out, Dataset(dataset.metadata, records))
    }
    else {
      push(out, dataset)
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

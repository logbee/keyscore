package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.Locale.{ENGLISH, GERMAN}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic.fieldListParameter
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, LogicParameters}
import io.logbee.keyscore.model.ToOption.T2OptionT
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Record}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Localization, TextRef}

object AddFieldsFilterLogic extends Described {

  private[pipeline] val fieldListParameter = FieldListParameterDescriptor(
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
      ref = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610",
      describes = FilterDescriptor(
        name = classOf[AddFieldsFilterLogic].getName,
        displayName = TextRef("displayName"),
        description = TextRef("description"),
        categories = Seq(TextRef("category")),
        parameters = Seq(fieldListParameter)
      ),
      localization = Localization.fromResourceBundle(
        bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilter",
        ENGLISH, GERMAN)
    )
}

class AddFieldsFilterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldsToAdd = Seq.empty[Field]

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {

    fieldsToAdd = configuration.getValueOrDefault(fieldListParameter, fieldsToAdd)
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

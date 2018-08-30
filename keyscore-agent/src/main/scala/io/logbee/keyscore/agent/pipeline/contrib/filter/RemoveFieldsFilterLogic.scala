package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.Locale

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, LogicParameters}
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.ToOption.T2OptionT
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Localization, TextRef}

import scala.Int.MaxValue


object RemoveFieldsFilterLogic extends Described {

  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RemoveFieldsFilter"

  private val fieldsToRemoveParameter = FieldNameListParameterDescriptor(
    ref = "removeFields.removeFields",
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
    uuid = "b7ee17ad-582f-494c-9f89-2c9da7b4e467",
    describes = FilterDescriptor(
      name = classOf[RemoveFieldsFilterLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq("category"),
      parameters = Seq(fieldsToRemoveParameter)
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RemoveFieldsFilter",
      Locale.ENGLISH, Locale.GERMAN
    )
  )
}

class RemoveFieldsFilterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldsToRemove = Seq.empty[String]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    fieldsToRemove = configuration.getValueOrDefault(RemoveFieldsFilterLogic.fieldsToRemoveParameter, fieldsToRemove)
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

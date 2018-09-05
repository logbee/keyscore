package io.logbee.keyscore.agent.pipeline.contrib.filter


import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.contrib.filter.RetainFieldsFilterLogic.fieldNamesParameter
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, LogicParameters}
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Record}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT

import scala.Int.MaxValue

object RetainFieldsFilterLogic extends Described {

  private[filter] val fieldNamesParameter = FieldNameListParameterDescriptor(
    ref = "retain.fieldNames",
    info = ParameterInfo(TextRef("fieldNamesDisplayName"), TextRef("fieldNamesDescription")),
    descriptor = FieldNameParameterDescriptor(
      hint = PresentField
    ),
    min = 1,
    max = MaxValue
  )

  override def describe = Descriptor(
    ref = "99f4aa2a-ee96-4cf9-bda5-261efb3a8ef6",
    describes = FilterDescriptor(
      name = classOf[RetainFieldsFilterLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(TextRef("category")),
      parameters = Seq(fieldNamesParameter)
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RetainFieldsFilter",
      Locale.ENGLISH, Locale.GERMAN
    )
  )
}

class RetainFieldsFilterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldsToRetain = Seq.empty[String]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    fieldsToRetain = configuration.getValueOrDefault(fieldNamesParameter, fieldsToRetain)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)
    val records = dataset.records.map( record => {
      Record(record.fields.filter(field => fieldsToRetain.contains(field.name)))
    })

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record}

import scala.collection.mutable

object RemoveFieldsFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RemoveFieldsFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RemoveFieldsFilter"
  private val filterId = "b7ee17ad-582f-494c-9f89-2c9da7b4e467"

  override def describe: MetaFilterDescriptor = {
    val descriptorMap = mutable.Map.empty[Locale, FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(fromString(filterId), filterName, descriptorMap.toMap)
  }

  private def descriptor(language: Locale): FilterDescriptorFragment = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(bundleName, language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = true),
      parameters = List(
        ListParameterDescriptor("fieldsToRemove", translatedText.getString("fieldsToRemoveName"), translatedText.getString("fieldsToRemoveDescription"),
          TextParameterDescriptor("fieldName", translatedText.getString("fieldKeyName"), translatedText.getString("fieldKeyDescription")))
      ))
  }
}

class RemoveFieldsFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  private var fieldsToRemove = List.empty[String]

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters)
      parameter match {
        case TextListParameter("fieldsToRemove", value) => fieldsToRemove = value
        case _ =>
      }
  }

  override def onPush(): Unit = {

    val dataset = grab(in)
    val records = dataset.records.map( record => {
      Record(record.fields.filter(field => !fieldsToRemove.contains(field.name)))
    })

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

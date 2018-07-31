package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable

object AddFieldsFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilter"
  private val filterId = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610"

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
      previousConnection = FilterConnection(true),
      nextConnection = FilterConnection(true),
      parameters = List(
        MapParameterDescriptor("fieldsToAdd", translatedText.getString("fieldsToAddName"), translatedText.getString("fieldsToAddDescription"),
          TextParameterDescriptor("fieldName", translatedText.getString("fieldKeyName"), translatedText.getString("fieldKeyDescription")),
          TextParameterDescriptor("fieldValue", translatedText.getString("fieldValueName"), translatedText.getString("fieldValueDescription"))
        )
      ))
  }
}

class AddFieldsFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  private var fieldsToAdd = List.empty[Field]

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters) {
      parameter.name match {
        case "fieldsToAdd" =>
          fieldsToAdd = parameter.value.asInstanceOf[Map[String, String]].foldLeft(List[Field]()) {
            case (list, (name, value)) => list :+ Field(name, TextValue(value))
            case (list, _) => list
          }
        case _ =>
      }
    }
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    if (fieldsToAdd.nonEmpty) {
      val records = dataset.records.map(record => Record(fields = fieldsToAdd ++ record.fields))
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

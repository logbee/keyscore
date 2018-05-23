package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RemoveFieldsFilterFunction extends Described {

  private val filterName= "io.logbee.keyscore.agent.stream.contrib.filter.RemoveFieldsFilter"
  private val filterId = "b7ee17ad-582f-494c-9f89-2c9da7b4e467"

  override def describe:MetaFilterDescriptor= {
    val descriptorMap = mutable.Map.empty[Locale,FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(fromString(filterId), filterName, descriptorMap.toMap)
  }

  private def descriptor(language:Locale): FilterDescriptorFragment = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(filterName,language)
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

class RemoveFieldsFilterFunction extends FilterFunction {
  var fieldsToRemove = List[String]()

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters)
      parameter.name match {
        case "fieldsToRemove" =>
          fieldsToRemove = parameter.value.asInstanceOf[List[String]]
        case _ =>
      }
  }

  override def apply(dataset: Dataset): Dataset = {
    var listBufferOfRecords = ListBuffer[Record]()
    for (record <- dataset) {
      var payload = record.payload.filterKeys(!fieldsToRemove.contains(_))
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    new Dataset(listOfRecords)
  }
}

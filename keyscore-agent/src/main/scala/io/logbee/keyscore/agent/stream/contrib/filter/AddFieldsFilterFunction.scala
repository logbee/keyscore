package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object AddFieldsFilterFunction extends Described {

  private val filterName = "io.logbee.keyscore.agent.stream.contrib.filter.AddFieldsFilter"
  private val filterId = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610"

  override def descriptors: MetaFilterDescriptor = {
    val descriptorMap = mutable.Map.empty[Locale,FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )
    MetaFilterDescriptor(fromString(filterId), filterName, descriptorMap.toMap)
  }


  private def descriptor(language: Locale): FilterDescriptorFragment = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(filterName, language)
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

class AddFieldsFilterFunction extends FilterFunction {
  var dataToAdd = scala.collection.mutable.Map[String, Field[_]]()

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters) {
      parameter.name match {
        case "fieldsToAdd" =>
          val dataMap = parameter.value.asInstanceOf[Map[String, String]]
          dataToAdd ++= dataMap.map(pair => (pair._1, TextField(pair._1, pair._2)))
        case _ =>
      }
    }
  }

  override def apply(dataset: Dataset): Dataset = {
    var listBufferOfRecords = ListBuffer[Record]()
    for (record <- dataset) {
      var payload = new mutable.HashMap[String, Field[_]]()
      payload ++= record.payload
      payload ++= dataToAdd
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    new Dataset(listOfRecords)
  }
}

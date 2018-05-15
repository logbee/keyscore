package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle, UUID}

import io.logbee.keyscore.agent.stream.contrib.filter.AddFieldsFilterFunction.descriptors
import io.logbee.keyscore.model.{sink, _}
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer





object AddFieldsFilterFunction extends Described {

  val meta = new MetaFilterDescriptor(fromString("1a6e5fd0-a21b-4056-8a4a-399e3b4e7610"), "AddFiledFilter", descriptors.toMap)

  override def descriptors: mutable.Map[Locale, sink.FilterDescriptor] = {
    val descriptors = mutable.Map.empty[Locale,sink.FilterDescriptor]
    descriptors ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )
  }

  private def descriptor(language: Locale): sink.FilterDescriptor = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(meta.name, language)
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
  var dataToAdd = scala.collection.mutable.Map[String, Field]()

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
      var payload = new mutable.HashMap[String, Field]()
      payload ++= record.payload
      payload ++= dataToAdd
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    new Dataset(listOfRecords)
  }
}

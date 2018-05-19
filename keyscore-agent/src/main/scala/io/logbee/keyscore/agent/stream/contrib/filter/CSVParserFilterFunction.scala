package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.{Locale, ResourceBundle, UUID}

import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object CSVParserFilterFunction extends Described {

  private val filterName = "io.logbee.keyscore.agent.stream.contrib.filter.CSVParserFilter"
  private val filterId = "292d368e-6e50-4c52-aed5-1a6826d78c22"

  override def descriptors: MetaFilterDescriptor = {
    val descriptorMap = mutable.Map.empty[Locale,FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(UUID.fromString(filterId),filterName,descriptorMap.toMap)
  }

  private def descriptor(language:Locale):FilterDescriptorFragment = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(filterName,language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = true),
      parameters = List(
        ListParameterDescriptor("headers",
          translatedText.getString("headerToParse"),
          translatedText.getString("headerToParseDescription"),
          TextParameterDescriptor("headerName",translatedText.getString("fieldKeyNameHeader"), translatedText.getString("fieldKeyDescriptionHeader"))
        ),
        TextParameterDescriptor("separator",translatedText.getString("fieldKeyNameSeparator"), translatedText.getString("fieldKeyDescriptionSeparator"))
      ))
  }
}

class CSVParserFilterFunction extends FilterFunction {
  var headerList : List[String] = List[String]()
  var separator : String = ""

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters)
      parameter.name match {
        case "separator" =>
          separator = parameter.value.asInstanceOf[String]
        case "headers" =>
          headerList = parameter.value.asInstanceOf[List[String]]
        case _ =>
      }

  }

  override def apply(dataset: Dataset): Dataset = {
    var recordsList =  ListBuffer[Record]()
    for (record <- dataset){
      for (field <- record.payload.values) {
        val message = field.asInstanceOf[TextField].value
        val listOfData = message.split(separator).map( x => TextField(x,x)).toList
        val dataMap : Map[String, TextField] = headerList.zip(listOfData).toMap
        val rec = Record(dataMap)
        recordsList += rec
      }
    }

    val dataList = recordsList.toList
    new Dataset(dataList)
  }
}

package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.{Locale, ResourceBundle, UUID}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.stream.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object CSVParserFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.stream.contrib.filter.CSVParserFilter"
  private val filterId = "292d368e-6e50-4c52-aed5-1a6826d78c22"

  override def describe: MetaFilterDescriptor = {
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

class CSVParserFilterLogic(context:StageContext,configuration:FilterConfiguration,shape:FlowShape[Dataset,Dataset]) extends FilterLogic(context,configuration,shape) {
  var headerList : List[String] = List[String]()
  var separator : String = ""

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

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

  override def onPush(): Unit = {
    val dataset = grab(shape.in)

    var recordsList =  ListBuffer[Record]()
    for (record <- dataset){
      for (field <- record.payload.values) {
        val message = field.asInstanceOf[TextField].value
        val listOfData = message.split(separator).map( x => TextField(x,x)).toList
        val dataMap : Map[String, TextField] = headerList.zip(listOfData).toMap.map(e => e._1 -> TextField(e._1.toString,e._2.value))
        val rec = Record(record.id,dataMap)
        recordsList += rec
      }
    }

    val dataList = recordsList.toList
    push(out,new Dataset(dataList))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

package io.logbee.keyscore.agent.stream.contrib.filter

import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable.ListBuffer

object CSVParserFilterFunction extends Described {
  override def descriptor: FilterDescriptor = FilterDescriptor(
    name = "CSVFilter",
    description = "Filter that parses csv in a readable format with key and value.",
    previousConnection = FilterConnection(true),
    nextConnection = FilterConnection(true),
    parameters = List(
      ListParameterDescriptor("headers",
        TextParameterDescriptor("headerName"),
        min = 1),
      TextParameterDescriptor("separator")
  ))
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

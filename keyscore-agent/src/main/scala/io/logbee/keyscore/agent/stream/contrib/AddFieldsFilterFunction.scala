package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object AddFieldsFilterFunction extends Described {
  override def descriptor: FilterDescriptor = FilterDescriptor("AddFields", "Filter that adds fields with specified data.", List(
    MapParameterDescriptor("fieldsToAdd", TextParameterDescriptor("fieldName"), TextParameterDescriptor("fieldValue"), min = 1)
  ))
}

class AddFieldsFilterFunction extends FilterFunction {
  var dataToAdd = scala.collection.mutable.Map[String, Field]()

  override def configure(configuration: FilterConfiguration): Boolean = {
    for (parameter <- configuration.parameters) {
      parameter.name match {
        case "fieldsToAdd" =>
          val dataMap = parameter.value.asInstanceOf[Map[String, String]]
          dataToAdd ++= dataMap.map(pair => (pair._1, TextField(pair._1, pair._2)))
      }
    }
    true
  }

  override def apply(dataset: Dataset): Dataset = {
    var listBufferOfRecords =  ListBuffer[Record]()
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

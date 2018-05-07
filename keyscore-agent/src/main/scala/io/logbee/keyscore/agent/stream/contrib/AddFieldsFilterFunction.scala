package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object AddFieldsFilterFunction extends Described {
  override def descriptor: FilterDescriptor = FilterDescriptor(
    name = "AddFieldsFilter",
    description = "Filter that adds fields with specified data.",
    previousConnection = FilterConnection(true),
    nextConnection = FilterConnection(true),
    parameters = List(
      MapParameterDescriptor("fieldsToAdd",
        TextParameterDescriptor("fieldName"),
        TextParameterDescriptor("fieldValue"),
        min = 1)
    )
  )
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

package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model.{Dataset, Described, Record}
import io.logbee.keyscore.model.filter._

import scala.collection.mutable.ListBuffer

object RemoveFieldsFilterFunction extends Described {



  override def descriptor: FilterDescriptor = {
    val fieldsToRemoveParameter = ListParameterDescriptor("fieldsToRemove", TextParameterDescriptor("fieldName"), min = 1)
    FilterDescriptor("RemoveFieldsFilter", "removing specified fields", List(
      fieldsToRemoveParameter
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
    var listBufferOfRecords =  ListBuffer[Record]()
    for (record <- dataset) {
      var payload = record.payload.filterKeys(!fieldsToRemove.contains(_))
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    new Dataset(listOfRecords)
  }
}

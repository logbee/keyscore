package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model.{Dataset, Described, Record}
import io.logbee.keyscore.model.filter._

import scala.collection.mutable.ListBuffer

object RetainFieldsFilterFunction extends Described {
  override def descriptor: FilterDescriptor = FilterDescriptor("RetainFieldsFilter", "retaining only specified fields", List(
    ListParameterDescriptor("fieldsToRetain", TextParameterDescriptor("fieldName"), min = 1)
  ))
}

class RetainFieldsFilterFunction extends FilterFunction{
  var fieldsToRetain = List[String]()

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters)
      parameter.name match {
        case "fieldsToRetain" =>
          fieldsToRetain = parameter.value.asInstanceOf[List[String]]
        case _ =>
      }

  }

  override def apply(dataset: Dataset): Dataset = {
    var listBufferOfRecords =  ListBuffer[Record]()
    for (record <- dataset) {
      var payload = record.payload.filterKeys(fieldsToRetain.contains(_))
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    new Dataset(listOfRecords)
  }
}

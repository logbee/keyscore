package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.Locale

import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record, sink}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RemoveFieldsFilterFunction extends Described {

  override def descriptors: mutable.Map[Locale, sink.FilterDescriptor] = {
    val descriptors = mutable.Map.empty[Locale,sink.FilterDescriptor]
    descriptors ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH)
    )
  }

  private def descriptor(language:Locale): sink.FilterDescriptor = FilterDescriptor(
    name = "RemoveFieldsFilter",
    description = "Removes all given fields and their values.",
    previousConnection = FilterConnection(true),
    nextConnection = FilterConnection(true),
    parameters = List(
      ListParameterDescriptor("fieldsToRemove",
        TextParameterDescriptor("fieldName"),
        min = 1)
    ))
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

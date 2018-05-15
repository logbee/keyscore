package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.Locale

import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record, sink}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RetainFieldsFilterFunction extends Described {

  override def descriptors: mutable.Map[Locale, sink.FilterDescriptor] = {
    val descriptors = mutable.Map.empty[Locale,sink.FilterDescriptor]
    descriptors ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH)
    )
  }

  private def descriptor(language:Locale): sink.FilterDescriptor = FilterDescriptor(
    name = "RetainFieldsFilter",
    description = "Retains only the given fields and their values and removes the other fields.",
    previousConnection = FilterConnection(true),
    nextConnection = FilterConnection(true),
    parameters = List(
      ListParameterDescriptor("fieldsToRetain",
        TextParameterDescriptor("fieldName"),
        min = 1)
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

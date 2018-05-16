package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.Locale
import java.util.UUID.fromString

import io.logbee.keyscore.agent.stream.contrib.filter.AddFieldsFilterFunction.{filterId, filterName}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record, sink}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RetainFieldsFilterFunction extends Described {

  val filterName = "RetainFieldsFilter"
  val filterId = "99f4aa2a-ee96-4cf9-bda5-261efb3a8ef6"

  override def descriptors: MetaFilterDescriptor = {
    val descriptorMap = mutable.Map.empty[Locale, FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH)
    )

    MetaFilterDescriptor(fromString(filterId), filterName, descriptorMap.toMap)

  }

  private def descriptor(language: Locale): FilterDescriptorFragment = FilterDescriptorFragment(
    displayName = "Retain Fields Filter",
    description = "Retains only the given fields and their values and removes the other fields.",
    previousConnection = FilterConnection(true),
    nextConnection = FilterConnection(true),
    parameters = List(
      ListParameterDescriptor("fieldsToRetain",
        TextParameterDescriptor("fieldName"),
        min = 1)
    ))
}

class RetainFieldsFilterFunction extends FilterFunction {
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
    var listBufferOfRecords = ListBuffer[Record]()
    for (record <- dataset) {
      var payload = record.payload.filterKeys(fieldsToRetain.contains(_))
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    new Dataset(listOfRecords)
  }
}

package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, FilterFunction}

import scala.Function.tupled
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex


object GrokFilterFunction extends Described {
  override def descriptor: FilterDescriptor = FilterDescriptor("GrokFilter", "Filter that structures messages with Regex.", List.empty)
}

class GrokFilterFunction extends FilterFunction {
  private val GROK_PATTERN: Regex = "\\(\\?<(\\w*)>".r
  private val NUMBER_PATTERN: Regex = "^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$".r

  private var fieldNames = List.empty[String]
  private var regex: Regex = "".r

  override def configure(configuration: FilterConfiguration): Boolean = {
    for (parameter <- configuration.parameters){
      parameter.name match {
        case "fieldNames" => fieldNames = parameter.value.asInstanceOf[List[String]]
        case "pattern" => parameter.value match {
          case Some(pattern) =>
            regex = pattern.r(GROK_PATTERN.findAllMatchIn(pattern).map(_.group(1)).toSeq: _*)
          case None =>
        }
      }
    }
  }

  override def apply(dataset: Dataset): Dataset = {
    val payload = new mutable.HashMap[String, Field]
    var listBufferOfRecords =  ListBuffer[Record]()
    for (record <- dataset) {
      for (field <- record.payload.values) {
        payload.put(field.name, field)
        if (fieldNames.contains(field.name) && field.isInstanceOf[TextField]) {
          regex.findFirstMatchIn(field.asInstanceOf[TextField].value)
            .foreach(patternMatch => patternMatch.groupNames.map(name => (name, patternMatch.group(name))) map tupled { (name, value) =>
              value match {
                case NUMBER_PATTERN(_*) => NumberField(name, BigDecimal(value))
                case _ => TextField(name, value)
              }
            } foreach (field => payload.put(field.name, field)))
        }
      }
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    new Dataset(listOfRecords)
  }
}

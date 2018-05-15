package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.Locale

import io.logbee.keyscore.model.{sink, _}
import io.logbee.keyscore.model.filter._

import scala.Function.tupled
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex


object GrokFilterFunction extends Described {
  override def descriptors: mutable.Map[Locale,sink.FilterDescriptor] = {
    val descriptors = mutable.Map.empty[Locale,sink.FilterDescriptor]
    descriptors ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH)
    )
  }

  private def descriptor(language:Locale) = {
    FilterDescriptor(
      name = "GrokFilter",
      description = "Extracts parts of a text line into fields.",
      previousConnection = FilterConnection(true),
      nextConnection = FilterConnection(true),
      parameters = List(
        BooleanParameterDescriptor("isPaused"),
        ListParameterDescriptor("fieldNames",
          TextParameterDescriptor("field"),
          min = 1),
        TextParameterDescriptor("pattern")
      ))
  }
}

class GrokFilterFunction extends FilterFunction {
  private val GROK_PATTERN: Regex = "\\(\\?<(\\w*)>".r
  private val NUMBER_PATTERN: Regex = "^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$".r

  private var fieldNames = List.empty[String]
  private var regex: Regex = "".r

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters) {
      parameter.name match {
        case "fieldNames" => fieldNames = parameter.value.asInstanceOf[List[String]]
        case "pattern" => parameter.value match {
          case Some(pattern: String) =>
            regex = pattern.r(GROK_PATTERN.findAllMatchIn(pattern).map(_.group(1)).toSeq: _*)
          case None =>
        }
        case _ =>
      }
    }
  }

  override def apply(dataset: Dataset): Dataset = {
    var listBufferOfRecords =  ListBuffer[Record]()
    for (record <- dataset) {
      var payload = new mutable.HashMap[String, Field]
      payload ++= record.payload
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

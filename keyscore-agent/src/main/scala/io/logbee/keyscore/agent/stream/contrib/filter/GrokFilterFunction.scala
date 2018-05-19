package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.{Locale, ResourceBundle, UUID}

import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.Function.tupled
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex


object GrokFilterFunction extends Described {

  private val filterName = "io.logbee.keyscore.agent.stream.contrib.filter.GrokFilter"
  private val filterId = "8912a691-e982-4680-8fc7-fea6803fcef0"

  override def descriptors: MetaFilterDescriptor = {
    val descriptorMap = mutable.Map.empty[Locale,FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(UUID.fromString(filterId),filterName,descriptorMap.toMap)
  }

  private def descriptor(language:Locale) = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(filterName, language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(true),
      nextConnection = FilterConnection(true),
      parameters = List(
        BooleanParameterDescriptor("isPaused",translatedText.getString("displayNameBoolean"), translatedText.getString("descriptionBoolean")),
        ListParameterDescriptor("fieldNames",translatedText.getString("fieldNames"),translatedText.getString("fieldNamesDescription"),
          TextParameterDescriptor("field",translatedText.getString("fieldKeyNameHeader"), translatedText.getString("fieldKeyDescriptionHeader"))),
        TextParameterDescriptor("pattern",translatedText.getString("patternKeyNameHeader"), translatedText.getString("patternKeyDescriptionHeader"))
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
      var payload = new mutable.HashMap[String, Field[_]]
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

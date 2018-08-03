package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.{Locale, ResourceBundle, UUID}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.util.matching.Regex


object GrokFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.GrokFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.GrokFilter"
  private val filterId = "8912a691-e982-4680-8fc7-fea6803fcef0"

  override def describe: MetaFilterDescriptor = {
    val fragments = Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(UUID.fromString(filterId), filterName, fragments)
  }

  private def descriptor(language: Locale) = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(bundleName, language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = true),
      parameters = List(
        ListParameterDescriptor("fieldNames", translatedText.getString("fieldNames"), translatedText.getString("fieldNamesDescription"),
          TextParameterDescriptor("field", translatedText.getString("fieldKeyNameHeader"), translatedText.getString("fieldKeyDescriptionHeader"))),
        TextParameterDescriptor("pattern", translatedText.getString("patternKeyNameHeader"), translatedText.getString("patternKeyDescriptionHeader"))
      ))
  }
}

class GrokFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  private val GROK_PATTERN: Regex = "\\(\\?<(\\w*)>".r
  private val NUMBER_PATTERN: Regex = "^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$".r

  private var fieldNames = List.empty[String]
  private var regex: Regex = "".r

  override def configure(configuration: FilterConfiguration): Unit = {
    configuration.parameters.foreach {
      case TextListParameter("fieldNames", value) => fieldNames = value
      case TextParameter("pattern", value) => regex = value.r(GROK_PATTERN.findAllMatchIn(value).map(_.group(1)).toSeq: _*)
      case _ =>
    }
  }

  override def onPush(): Unit = {
    push(out, grok(grab(in)))
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def grok(dataset: Dataset): Dataset = {

    Dataset(dataset.metadata, dataset.records.map(record => {
      val fields = mutable.ListBuffer[Field]() ++= record.fields
      for (field <- record.fields) {
        if (fieldNames.contains(field.name) && field.value.isInstanceOf[TextValue]) {
          regex.findFirstMatchIn(field.value.asInstanceOf[TextValue].value).foreach(patternMatch =>
            patternMatch.groupNames.map(name => {
                patternMatch.group(name) match {
                  case value @ NUMBER_PATTERN(_*) => Field(name, DecimalValue(value.toDouble))
                  case value => Field(name, TextValue(value))
                }
            }).foldLeft(fields) { (fields, field) => fields += field })
        }
      }
      Record(fields.toList)
    }))
  }
}

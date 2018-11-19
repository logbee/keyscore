package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.filter.GrokLogic.fieldNamesParameter

import scala.Int.MaxValue
import scala.collection.mutable
import scala.util.matching.Regex

object GrokLogic extends Described {

  private[filter] val fieldNamesParameter = FieldNameListParameterDescriptor(
    ref = "grok.fieldNames",
    info = ParameterInfo(
      displayName = TextRef("fieldNames"),
      description = TextRef("fieldNamesDescription")
    ),
    descriptor = FieldNameParameterDescriptor(
      hint = PresentField
    ),
    min = 1,
    max = MaxValue
  )

  private[filter] val patternParameter = ExpressionParameterDescriptor(
    ref = "grok.pattern",
    info = ParameterInfo(
      displayName = TextRef("patternKeyNameHeader"),
      description = TextRef("patternKeyDescriptionHeader")
    ),
    expressionType = ExpressionType.Grok,
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "8912a691-e982-4680-8fc7-fea6803fcef0",
    describes = FilterDescriptor(
      name = classOf[GrokLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.DATA_EXTRACTION),
      parameters = Seq(fieldNamesParameter, patternParameter),
      icon = Icon.fromClass(classOf[GrokLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.Grok",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class GrokLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private val GROK_PATTERN: Regex = "\\(\\?<(\\w*)>".r
  private val NUMBER_PATTERN: Regex = "^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$".r

  private var fieldNames = Seq.empty[String]
  private var pattern = ""
  private var regex: Regex = "".r

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    fieldNames = configuration.getValueOrDefault(fieldNamesParameter, fieldNames)
    pattern = configuration.getValueOrDefault(GrokLogic.patternParameter, pattern)
    regex = pattern.r(GROK_PATTERN.findAllMatchIn(pattern).map(_.group(1)).toSeq: _*)
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
                case value@NUMBER_PATTERN(_*) => Field(name, DecimalValue(value.toDouble))
                case value => Field(name, TextValue(value))
              }
            }).foldLeft(fields) { (fields, field) => fields += field })
        }
      }
      Record(fields.toList)
    }))
  }
}

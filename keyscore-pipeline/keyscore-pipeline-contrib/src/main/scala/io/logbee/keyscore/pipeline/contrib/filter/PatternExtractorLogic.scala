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
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.filter.PatternExtractorLogic.fieldNamesParameter

import scala.Int.MaxValue
import scala.collection.mutable
import scala.util.matching.Regex

object PatternExtractorLogic extends Described {

  val fieldNamesParameter = FieldNameListParameterDescriptor(
    ref = "grok.fieldNames",
    info = ParameterInfo(
      displayName = TextRef("pattern-extractor.fieldNames.displayName"),
      description = TextRef("pattern-extractor.fieldNames.description")
    ),
    descriptor = FieldNameParameterDescriptor(
      hint = PresentField
    ),
    min = 1,
    max = MaxValue
  )

  val patternParameter = ExpressionParameterDescriptor(
    ref = "grok.pattern",
    info = ParameterInfo(
      displayName = TextRef("pattern-extractor.pattern.displayName"),
      description = TextRef("pattern-extractor.pattern.description")
    ),
    choices = Seq(
      Choice(
        name = "pattern-extractor",
        displayName = TextRef("pattern-extractor.pattern.named-capture.displayName"),
        description = TextRef("pattern-extractor.pattern.named-capture.description")
      )
    ),
    mandatory = true
  )

  val autoDetectParameter = BooleanParameterDescriptor(
    ref = "grok.autodetect",
    info = ParameterInfo(
      displayName = TextRef("pattern-extractor.autodetect.displayName"),
      description = TextRef("pattern-extractor.autodetect.description")
    ),
    defaultValue = false,
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "8912a691-e982-4680-8fc7-fea6803fcef0",
    describes = FilterDescriptor(
      name = classOf[PatternExtractorLogic].getName,
      displayName = TextRef("pattern-extractor.displayName"),
      description = TextRef("pattern-extractor.description"),
      categories = Seq(CommonCategories.DATA_EXTRACTION),
      parameters = Seq(fieldNamesParameter, patternParameter, autoDetectParameter),
      icon = Icon.fromClass(classOf[PatternExtractorLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.PatternExtractorLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class PatternExtractorLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private val CAPTURE_GROUPS_PATTERN: Regex = "\\(\\?<(\\w*)>".r
  private val NUMBER_PATTERN: Regex = "^[+-]?[\\d]+$".r
  private val DECIMAL_PATTERN: Regex = "^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$".r
  private val BOOLEAN_PATTERN: Regex = "^[Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee]$".r

  private var fieldNames = Seq.empty[String]
  private var pattern = ""
  private var regex: Regex = "".r
  private var autoDetect = PatternExtractorLogic.autoDetectParameter.defaultValue

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    fieldNames = configuration.getValueOrDefault(fieldNamesParameter, fieldNames)
    pattern = configuration.getValueOrDefault(PatternExtractorLogic.patternParameter, pattern)
    autoDetect = configuration.getValueOrDefault(PatternExtractorLogic.autoDetectParameter, autoDetect)
    regex = pattern.r(CAPTURE_GROUPS_PATTERN.findAllMatchIn(pattern).map(_.group(1)).toSeq: _*)
  }

  override def onPush(): Unit = {
    push(out, capture(grab(in)))
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def capture(dataset: Dataset): Dataset = {

    Dataset(dataset.metadata, dataset.records.map(record => {
      val fields = mutable.ListBuffer[Field]() ++= record.fields
      for (field <- record.fields) {
        if (fieldNames.contains(field.name) && field.value.isInstanceOf[TextValue]) {
          regex.findFirstMatchIn(field.value.asInstanceOf[TextValue].value).foreach(patternMatch =>
            patternMatch.groupNames.map(name => {
              patternMatch.group(name) match {
                case value@BOOLEAN_PATTERN(_*) if autoDetect => Field(name, BooleanValue(value.toBoolean))
                case value@NUMBER_PATTERN(_*) if autoDetect => Field(name, NumberValue(value.toLong))
                case value@DECIMAL_PATTERN(_*) if autoDetect => Field(name, DecimalValue(value.toDouble))
                case value => Field(name, TextValue(value))
              }
            }).foldLeft(fields) { (fields, field) => fields += field })
        }
      }
      Record(fields.toList)
    }))
  }
}

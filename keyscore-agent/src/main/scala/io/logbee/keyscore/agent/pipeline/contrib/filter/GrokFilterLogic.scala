package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.Locale

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.contrib.filter.GrokFilterLogic.fieldNamesParameter
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, LogicParameters}
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.ToOption.T2OptionT
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Localization, TextRef}

import scala.Int.MaxValue
import scala.collection.mutable
import scala.util.matching.Regex

object GrokFilterLogic extends Described {

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
      name = classOf[GrokFilterLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(TextRef("category")),
      parameters = Seq(fieldNamesParameter, patternParameter)
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.GrokFilter",
      Locale.ENGLISH, Locale.GERMAN
    )
  )

}

class GrokFilterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private val GROK_PATTERN: Regex = "\\(\\?<(\\w*)>".r
  private val NUMBER_PATTERN: Regex = "^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$".r

  private var fieldNames = Seq.empty[String]
  private var pattern = ""
  private var regex: Regex = "".r

  override def configure(configuration: Configuration): Unit = {

    fieldNames = configuration.getValueOrDefault(fieldNamesParameter, fieldNames)
    pattern = configuration.getValueOrDefault(GrokFilterLogic.patternParameter, pattern)
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

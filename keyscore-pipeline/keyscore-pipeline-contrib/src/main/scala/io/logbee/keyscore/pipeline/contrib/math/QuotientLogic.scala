package io.logbee.keyscore.pipeline.contrib.math

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.FieldNameHint.{AbsentField, PresentField}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.contrib.math.MathUtil.approximatelyEqual


object QuotientLogic extends Described {

  private[math] val xFieldNameParameter = FieldNameParameterDescriptor(
    ref = "quotient.xFieldName",
    info = ParameterInfo(
      displayName = TextRef("xFieldDisplayName"),
      description = TextRef("xFieldDescription"),
    ),
    hint = PresentField,
    defaultValue = "x",
    mandatory = true
  )

  private[math] val yFieldNameParameter = FieldNameParameterDescriptor(
    ref = "quotient.yFieldName",
    info = ParameterInfo(
      displayName = TextRef("yFieldDisplayName"),
      description = TextRef("yFieldDescription"),
    ),
    hint = PresentField,
    defaultValue = "y",
    mandatory = true
  )

  private[math] val targetFieldNameParameter = FieldNameParameterDescriptor(
    ref = "quotient.targetFieldName",
    info = ParameterInfo(
      displayName = TextRef("targetFieldDisplayName"),
      description = TextRef("targetFieldDescription"),
    ),
    hint = AbsentField,
    defaultValue = "quotient",
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "7f43984a-08c7-46ae-ac8b-40b5449eecc1",
    describes = FilterDescriptor(
      name = classOf[QuotientLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.MATH),
      parameters = List(xFieldNameParameter, yFieldNameParameter, targetFieldNameParameter),
      icon = Icon.fromClass(classOf[QuotientLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.math.QuotientLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}
class QuotientLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var xFieldName = QuotientLogic.xFieldNameParameter.defaultValue
  private var yFieldName = QuotientLogic.yFieldNameParameter.defaultValue
  private var targetFieldName = QuotientLogic.targetFieldNameParameter.defaultValue

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {
    xFieldName = configuration.getValueOrDefault(QuotientLogic.xFieldNameParameter, xFieldName)
    yFieldName = configuration.getValueOrDefault(QuotientLogic.yFieldNameParameter, xFieldName)
    targetFieldName = configuration.getValueOrDefault(QuotientLogic.targetFieldNameParameter, xFieldName)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    push(out, Dataset(dataset.metadata, dataset.records.map(record => {

      val x1Option = record.fields.find(field => xFieldName == field.name).flatMap(numericValue)
      val y1Option = record.fields.find(field => yFieldName == field.name).flatMap(numericValue)

      if (x1Option.isDefined && y1Option.isDefined) {

          val x = x1Option.get
          val y = y1Option.get

          if (!approximatelyEqual(y, 0)) {
            Record(record.fields :+ Field(targetFieldName, DecimalValue(x / y)))
          }
          else {
            record
          }
      }
      else {
        record
      }
    })))
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def numericValue(field: Field): Option[Double] = {
    field.value match {
      case DecimalValue(value, _) => value
      case NumberValue(value, _) => value.toDouble
      case _ => None
    }
  }
}

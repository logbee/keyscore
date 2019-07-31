package io.logbee.keyscore.pipeline.contrib.math

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.FieldNameHint.{AbsentField, PresentField}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.math.MathUtil.approximatelyEqual

object DifferentialQuotientLogic extends Described {

  private[math] val xFieldNameParameter = FieldNameParameterDescriptor(
    ref = "dqf.xFieldName",
    info = ParameterInfo(
      displayName = TextRef("xFieldDisplayName"),
      description = TextRef("xFieldDescription"),
    ),
    hint = PresentField,
    defaultValue = "x",
    mandatory = true
  )

  private[math] val yFieldNameParameter = FieldNameParameterDescriptor(
    ref = "dqf.yFieldName",
    info = ParameterInfo(
      displayName = TextRef("yFieldDisplayName"),
      description = TextRef("yFieldDescription"),
    ),
    hint = PresentField,
    defaultValue = "y",
    mandatory = true
  )

  private[math] val targetFieldNameParameter = FieldNameParameterDescriptor(
    ref = "dqf.targetFieldName",
    info = ParameterInfo(
      displayName = TextRef("targetFieldDisplayName"),
      description = TextRef("targetFieldDescription"),
    ),
    hint = AbsentField,
    defaultValue = "m",
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "a83715fd-bc0f-4012-9527-59c6d4a1f6cd",
    describes = FilterDescriptor(
      name = classOf[DifferentialQuotientLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.MATH),
      parameters = List(xFieldNameParameter, yFieldNameParameter, targetFieldNameParameter),
      icon = Icon.fromClass(classOf[DifferentialQuotientLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.math.DifferentialQuotientLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}
class DifferentialQuotientLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var xFieldName = DifferentialQuotientLogic.xFieldNameParameter.defaultValue
  private var yFieldName = DifferentialQuotientLogic.yFieldNameParameter.defaultValue
  private var targetFieldName = DifferentialQuotientLogic.targetFieldNameParameter.defaultValue

  private var lastValues: Option[(Double, Double)] = None

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {
    xFieldName = configuration.getValueOrDefault(DifferentialQuotientLogic.xFieldNameParameter, xFieldName)
    yFieldName = configuration.getValueOrDefault(DifferentialQuotientLogic.yFieldNameParameter, xFieldName)
    targetFieldName = configuration.getValueOrDefault(DifferentialQuotientLogic.targetFieldNameParameter, xFieldName)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    push(out, Dataset(dataset.metadata, dataset.records.map(record => {

      val x1Option = record.fields.find(field => xFieldName == field.name).flatMap(numericValue)
      val y1Option = record.fields.find(field => yFieldName == field.name).flatMap(numericValue)

      if (x1Option.isDefined && y1Option.isDefined) {

          val x1 = x1Option.get
          val y1 = y1Option.get

          if (lastValues.isDefined) {

            val x0 = lastValues.get._1
            val y0 = lastValues.get._2

            val x = x1 - x0

            if (!approximatelyEqual(x, 0)) {
              val m = (y1 - y0) / x
              lastValues = Option((x1, y1))
              Record(record.fields :+ Field(targetFieldName, DecimalValue(m)))
            }
            else {
              record
            }
          }
          else {
            lastValues = Option((x1, y1))
            Record(record.fields :+ Field(targetFieldName, DecimalValue(0)))
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
      case DecimalValue(value) => value
      case NumberValue(value) => value.toDouble
      case _ => None
    }
  }
}

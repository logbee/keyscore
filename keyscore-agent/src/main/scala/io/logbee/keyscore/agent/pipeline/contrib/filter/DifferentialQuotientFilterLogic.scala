package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.Locale

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.agent.pipeline.contrib.filter.DifferentialQuotientFilterLogic.{targetFieldNameParameter, xFieldNameParameter, yFieldNameParameter}
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.ToOption.T2OptionT
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, DecimalValue, Field, Record}
import io.logbee.keyscore.model.descriptor.FieldNameHint.{AbsentField, PresentField}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Localization, TextRef}

object DifferentialQuotientFilterLogic extends Described {

  private val xFieldNameParameter = FieldNameParameterDescriptor(
    ref = "dqf.xFieldName",
    info = ParameterInfo(
      displayName = TextRef("xFieldDisplayName"),
      description = TextRef("xFieldDescription"),
    ),
    hint = PresentField,
    defaultValue = "x",
    mandatory = true
  )

  private val yFieldNameParameter = FieldNameParameterDescriptor(
    ref = "dqf.yFieldName",
    info = ParameterInfo(
      displayName = TextRef("yFieldDisplayName"),
      description = TextRef("yFieldDescription"),
    ),
    hint = PresentField,
    defaultValue = "y",
    mandatory = true
  )

  private val targetFieldNameParameter = FieldNameParameterDescriptor(
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
    uuid = "a83715fd-bc0f-4012-9527-59c6d4a1f6cd",
    describes = FilterDescriptor(
        name = classOf[DifferentialQuotientFilterLogic].getName,
        displayName = TextRef("displayName"),
        description = TextRef("description"),
        categories = Seq(TextRef("categories")),
        parameters = List(xFieldNameParameter, yFieldNameParameter, targetFieldNameParameter)
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.DifferentialQuotientFilter",
      Locale.ENGLISH, Locale.GERMAN
    )
  )
}
class DifferentialQuotientFilterLogic(context: StageContext, configuration: Configuration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) with StageLogging {

  private var xFieldName = xFieldNameParameter.defaultValue
  private var yFieldName = yFieldNameParameter.defaultValue
  private var targetFieldName = targetFieldNameParameter.defaultValue

  private var lastValues: Option[(Double, Double)] = None

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {
    xFieldName = configuration.getValueOrDefault(xFieldNameParameter, xFieldName)
    yFieldName = configuration.getValueOrDefault(yFieldNameParameter, xFieldName)
    targetFieldName = configuration.getValueOrDefault(targetFieldNameParameter, xFieldName)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    push(out, Dataset(dataset.metadata, dataset.records.map(record => {

      val xField = record.fields.find(field => xFieldName == field.name)
      val yField = record.fields.find(field => yFieldName == field.name)

      if (xField.isDefined && xField.get.isNumberField && yField.isDefined && yField.get.isDecimalField) {

        val x1 = xField.get.toNumberField.value
        val y1 = yField.get.toDecimalField.value

        if (lastValues.isDefined) {

          val x0 = lastValues.get._1
          val y0 = lastValues.get._2

          val m = (y1 - y0) / (x1 - x0)

          lastValues = Option((x1, y1))
          Record(record.fields :+ Field(targetFieldName, DecimalValue(m)))
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
}
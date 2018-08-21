package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.{Locale, UUID}

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

object DifferentialQuotientFilterLogic extends Described {

  val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.DifferentialQuotientFilterLogic"
  val filterId: UUID = UUID.fromString("a83715fd-bc0f-4012-9527-59c6d4a1f6cd")

  override def describe: MetaFilterDescriptor = {
    MetaFilterDescriptor(filterId, filterName, Map(
      Locale.ENGLISH -> FilterDescriptorFragment(
        displayName = "Differential Quotient",
        description = "Computes the differential quotient based on the specified fields between two consecutive records.",
        previousConnection = FilterConnection(isPermitted = true),
        nextConnection = FilterConnection(isPermitted = true),
        category = "Math",
        parameters = List(
          TextParameterDescriptor(
            name = "xFieldName",
            displayName = "x-Field",
            description = "Name of the field containing the x value.",
            mandatory = true,
            validator = ".*"),
          TextParameterDescriptor(
            name = "yFieldName",
            displayName = "y-Field",
            description = "Name of the field containing the y value.",
            mandatory = true,
            validator = ".*"),
          TextParameterDescriptor(
            name = "targetFieldName",
            displayName = "Target Field",
            description = "Name of the field where the computed value should be stored.",
            mandatory = true,
            validator = ".*"
          )
        ))))
  }
}
class DifferentialQuotientFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) with StageLogging {

  private var xFieldName = "x"
  private var yFieldName = "y"
  private var targetFieldName = "m"

  private var lastValues: Option[(Double, Double)] = None

  override def initialize(configuration: FilterConfiguration): Unit = configure(configuration)

  override def configure(configuration: FilterConfiguration): Unit = {
    configuration.parameters.foreach {
      case TextParameter("xFieldName", value) => xFieldName = value
      case TextParameter("yFieldName", value) => yFieldName = value
      case TextParameter("targetFieldName", value) => targetFieldName = value
      case _ =>
    }
  }

  override def onPush(): Unit = {

    val dataset = grab(in)
    var outDataset = dataset

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

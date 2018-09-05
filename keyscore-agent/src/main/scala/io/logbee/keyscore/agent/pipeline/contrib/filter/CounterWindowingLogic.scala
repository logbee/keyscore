package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.{Locale, UUID}

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record}

import scala.collection.mutable

object CounterWindowingLogic extends Described {

  val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.CounterWindowing"
  val filterId: UUID = UUID.fromString("3bf6b11f-2fda-40a8-ab93-e3a71d6b132f")

  override def describe: MetaFilterDescriptor = {
    MetaFilterDescriptor(filterId, filterName, Map(
      Locale.ENGLISH -> FilterDescriptorFragment(
        displayName = "Counter Windowing",
        description = "Combines the specified amount of consecutive datasets into one.",
        previousConnection = FilterConnection(isPermitted = true),
        nextConnection = FilterConnection(isPermitted = true),
        category = "Windowing",
        parameters = List(
          IntParameterDescriptor("amount", "Amount", "The number of datasets to combine.")
        )
      )))
  }
}
class CounterWindowingLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) with StageLogging {

  private var amount = 1

  private val buffer = mutable.ListBuffer.empty[Dataset]

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    configuration.parameters.foreach {
      case IntParameter("amount", value) => amount = value
      case _ =>
    }
  }

  override def onPush(): Unit = {

    buffer += grab(in)

    if (buffer.size == amount) {

      push(out, Dataset(buffer.last.metadata, buffer.foldLeft(mutable.ListBuffer.empty[Record]) {
        case (result, dataset) => result ++ dataset.records
      }.toList))

      buffer.clear()
    }
    else {
      pull(in)
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

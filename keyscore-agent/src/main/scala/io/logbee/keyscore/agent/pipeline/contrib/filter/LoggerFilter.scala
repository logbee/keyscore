package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.{Locale, UUID}

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.{Dataset, Described}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterConnection, FilterDescriptorFragment, MetaFilterDescriptor}

object LoggerFilter extends Described {


  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.LoggerFilter"
  private val filterId = "9a6f5fd0-a21b-4056-7a2a-344e3b4e2488"

  override def describe = {
    MetaFilterDescriptor(UUID.fromString(filterId), filterName, Map(Locale.ENGLISH -> FilterDescriptorFragment(
      displayName = "Logger Filter",
      description = "",
      previousConnection = FilterConnection(true),
      nextConnection = FilterConnection(true),
      category = "debug"
    )))
  }
}

class LoggerFilter(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) with StageLogging {
  override def configure(configuration: FilterConfiguration): Unit = {}

  override def onPush(): Unit = {
    val dataset = grab(in)
    log.info(s"$dataset")
    push(out, dataset)
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

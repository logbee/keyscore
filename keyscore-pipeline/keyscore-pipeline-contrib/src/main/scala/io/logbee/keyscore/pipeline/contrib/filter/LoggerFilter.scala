package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, FilterDescriptor}
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}

object LoggerFilter extends Described {

  override def describe = Descriptor(
    ref = "634bce93-64a3-4469-a105-1be441fdc2e0",
    describes = FilterDescriptor(
      name = classOf[LoggerFilter].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(TextRef("category")),
      parameters = Seq()
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.LoggerFilter",
      Locale.ENGLISH, Locale.GERMAN
    )
  )
}

class LoggerFilter(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  override def initialize(configuration: Configuration): Unit = {}

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {
    val dataset = grab(in)
    log.info(s"$dataset")
    push(out, dataset)
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

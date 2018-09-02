package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.Locale

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, LogicParameters}
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.ToOption.T2OptionT
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, FilterDescriptor}
import io.logbee.keyscore.model.localization.{Localization, TextRef}

object LoggerFilter extends Described {

  override def describe = Descriptor(
    ref = "io.logbee.keyscore.agent.pipeline.contrib.filter.LoggerFilter",
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

  override def configure(configuration: Configuration): Unit = {
  }

  override def onPush(): Unit = {
    val dataset = grab(in)
    log.info(s"$dataset")
    push(out, dataset)
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

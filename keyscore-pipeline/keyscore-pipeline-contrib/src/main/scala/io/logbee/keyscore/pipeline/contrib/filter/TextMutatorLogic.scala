package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION


object TextMutatorLogic extends Described {
  override def describe = Descriptor(
    ref = "bf9c0ff2-64d5-44ed-9957-8128a50ab567",
    describes = FilterDescriptor(
      name = classOf[LoggerFilter].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.DEBUG),
      parameters = Seq()
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.TextMutatorFilter",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class TextMutatorLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

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

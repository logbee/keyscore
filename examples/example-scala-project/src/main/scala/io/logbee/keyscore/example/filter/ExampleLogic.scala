package io.logbee.keyscore.example.filter

import akka.stream.FlowShape
import io.logbee.keyscore.example.ExampleCategory
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Icon}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}


object ExampleLogic extends Described {

  override def describe = Descriptor(
      ref = "4a295991-7dcc-4911-9a7a-9d94573e6f50",
      describes = FilterDescriptor(
        name = classOf[ExampleLogic].getName,
        displayName = TextRef("displayName"),
        description = TextRef("description"),
        categories = Seq(ExampleCategory.EXAMPLE),
        parameters = Seq(),
        icon = Icon.fromClass(classOf[ExampleLogic])
      ),
      localization = Localization.fromResourceBundle(
        bundleName = "io.logbee.keyscore.example.filter.ExampleLogic",
        Locale.ENGLISH, Locale.GERMAN) ++ ExampleCategory.LOCALIZATION
    )
}

class ExampleLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {
      push(out, grab(in))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}

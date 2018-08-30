package io.logbee.keyscore.agent.pipeline

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, LogicParameters}
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, FilterDescriptor}

object ExampleFilter extends Described {

  private val filterId = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610"

  override def describe: Descriptor = {
    Descriptor(filterId,
      describes = FilterDescriptor(
        name = classOf[ExampleFilter].getName
      )
    )
  }
}
class ExampleFilter(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {}

  override def onPull(): Unit = {}
}
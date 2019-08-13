package io.logbee.keyscore.agent.runtimes.examples

import akka.stream.SinkShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, SinkDescriptor}
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}

object ExampleSink extends Described {

  private val sinkId = "a9596e11-d0f9-4f07-83f6-a73fdae1e5b2"

  override def describe: Descriptor = {
    Descriptor(sinkId,
      describes = SinkDescriptor(
        name = classOf[ExampleSink].getName
      )
    )
  }
}

class ExampleSink(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape) {
  override def onPush(): Unit = {
    pull(in)
  }

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {}
}

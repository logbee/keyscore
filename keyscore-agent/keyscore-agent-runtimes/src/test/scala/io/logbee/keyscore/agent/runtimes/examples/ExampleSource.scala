package io.logbee.keyscore.agent.runtimes.examples

import akka.stream.SourceShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, SourceDescriptor}
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}


object ExampleSource extends Described {

  private val sourceId = "4ce89cee-6f8f-453d-bb12-055602d4982b"

  override def describe: Descriptor = {
    Descriptor(sourceId,
      describes = SourceDescriptor(
        name = classOf[ExampleSource].getName
      )
    )
  }
}

class ExampleSource(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {

  override def configure(configuration: Configuration): Unit = {}

  override def onPull(): Unit = {
    push(out, Dataset())
  }

  override def initialize(configuration: Configuration): Unit = configure(configuration)
}

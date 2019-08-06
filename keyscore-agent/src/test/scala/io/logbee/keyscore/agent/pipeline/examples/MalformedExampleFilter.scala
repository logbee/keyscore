package io.logbee.keyscore.agent.pipeline.examples

import akka.stream.FlowShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, SourceDescriptor}
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}

object MalformedExampleFilter extends Described {

  private val sourceId = "a3018347-4b3a-435d-bef1-b85a493cb7d2"

  override def describe: Descriptor = {
    Descriptor(sourceId,
      describes = SourceDescriptor(
        name = classOf[MalformedExampleFilter].getName
      )
    )
  }
}

class MalformedExampleFilter(shape: FlowShape[Dataset, Dataset]) extends FilterLogic(LogicParameters(null, null, null, null), shape) {

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {
    push(out, grab(in))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
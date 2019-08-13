package io.logbee.keyscore.agent.runtimes.jvm.examples

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, MergeDescriptor}
import io.logbee.keyscore.pipeline.api.{LogicParameters, MergeLogic, MergeShape}


object ExampleMerge extends Described {

  private val mergeId = "7068ee5a-9efa-45d0-a6e4-cbc9411f8d49"

  override def describe: Descriptor = {
    Descriptor(mergeId,
      describes = MergeDescriptor(
      name = classOf[ExampleMerge].getName
      )
    )
  }
}

class ExampleMerge(parameters: LogicParameters, shape: MergeShape[Dataset, Dataset, Dataset]) extends MergeLogic(parameters, shape) {

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {}

  override def onPull(): Unit = {}
}

package io.logbee.keyscore.agent.runtimes.jvm.examples

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{BranchDescriptor, Descriptor}
import io.logbee.keyscore.pipeline.api.{BranchLogic, BranchShape, LogicParameters}

object ExampleBranch extends Described {

  private val branchId = "ecd253fc-fe90-45f0-b739-9004defb6554"

  override def describe: Descriptor = {
    Descriptor(branchId,
      describes = BranchDescriptor(
        name = classOf[ExampleBranch].getName
      )
    )
  }
}

class ExampleBranch(parameters: LogicParameters, shape: BranchShape[Dataset, Dataset, Dataset]) extends BranchLogic(parameters, shape) {

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {}

  override def onPull(): Unit = {}
}

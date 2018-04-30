package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described}

object ExampleFilterFunction extends Described {
  override val descriptor: FilterDescriptor = FilterDescriptor("ExampleFilter", "An Example Filter", List.empty)
}

class ExampleFilterFunction extends FilterFunction {

  override def configure(configuration: FilterConfiguration): Unit = ???

  override def apply(dataset: Dataset): Dataset = ???
}

package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.filter.{Filter, FilterDescriptor}

object ExampleFilter extends Described {
  override val descriptor: FilterDescriptor = FilterDescriptor("ExampleFilter", "An Example Filter", List.empty)
}

class ExampleFilter extends Filter {

}

package io.logbee.keyscore.model.sink

import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration

trait SinkFunction {

  def configure(configuration: FilterConfiguration): Unit

  def apply(dataset: Dataset): Unit
}

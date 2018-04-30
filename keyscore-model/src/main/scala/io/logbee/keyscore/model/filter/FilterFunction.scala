package io.logbee.keyscore.model.filter

import io.logbee.keyscore.model.Dataset

trait FilterFunction {

  def configure(configuration: FilterConfiguration): Unit

  def apply(dataset: Dataset): Dataset
}

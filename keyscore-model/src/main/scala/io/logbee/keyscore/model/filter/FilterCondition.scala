package io.logbee.keyscore.model.filter

import io.logbee.keyscore.model.Dataset

trait FilterCondition {

  def configure(configuration: FilterConfiguration): Boolean

  def apply(dataset: Dataset): FilterConditionResult
}

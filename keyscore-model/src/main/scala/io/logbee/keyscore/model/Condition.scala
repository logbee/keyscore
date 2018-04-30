package io.logbee.keyscore.model

import io.logbee.keyscore.model.filter.FilterConfiguration

trait Condition {

  def configure(configuration: FilterConfiguration): Boolean

  def apply(dataset: Dataset): ConditionResult
}

package io.logbee.keyscore.model

import io.logbee.keyscore.model.filter.FilterDescriptor

trait Described {
  val descriptor: FilterDescriptor
}

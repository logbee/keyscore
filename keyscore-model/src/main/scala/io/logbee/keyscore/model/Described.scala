package io.logbee.keyscore.model

import io.logbee.keyscore.model.filter.MetaFilterDescriptor


trait Described {
  def describe: MetaFilterDescriptor
}

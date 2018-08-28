package io.logbee.keyscore.model

import io.logbee.keyscore.model.descriptor.Descriptor


trait Described {
  def describe: Descriptor
}

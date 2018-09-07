package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.DescriptorRef
import org.json4s.CustomKeySerializer

object DescriptorRefKeySerializer extends CustomKeySerializer[DescriptorRef](format => ({
  case uuid: String => DescriptorRef(uuid)
}, {
  case ref: DescriptorRef => ref.uuid
}))

package io.logbee.keyscore.model.descriptor

import java.util.UUID

object ToDescriptorRef {
  implicit def stringToDescriptorRef(uuid: String): DescriptorRef = uuidToDescriptorRef(UUID.fromString(uuid))
  implicit def uuidToDescriptorRef(uuid: UUID): DescriptorRef = DescriptorRef(uuid.toString)
}

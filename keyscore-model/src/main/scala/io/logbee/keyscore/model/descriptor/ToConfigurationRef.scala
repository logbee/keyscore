package io.logbee.keyscore.model.descriptor

import java.util.UUID
import scala.language.implicitConversions

object ToDescriptorRef {
  implicit def stringToDescriptorRef(uuid: String): DescriptorRef = uuidToDescriptorRef(UUID.fromString(uuid))
  implicit def uuidToDescriptorRef(uuid: UUID): DescriptorRef = DescriptorRef(uuid.toString)
}

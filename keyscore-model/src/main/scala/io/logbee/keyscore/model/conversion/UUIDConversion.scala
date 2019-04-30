package io.logbee.keyscore.model.conversion

import java.util.UUID

import io.logbee.keyscore.model.blueprint.BlueprintRef
import io.logbee.keyscore.model.configuration.ConfigurationRef
import io.logbee.keyscore.model.descriptor.DescriptorRef
import scala.language.implicitConversions

object UUIDConversion {

  implicit def uuidToString(uuid: UUID): String = uuid.toString

  implicit def uuidFromString(uuid: String): UUID = UUID.fromString(uuid)

  implicit def uuidFromBlueprintRef(ref: BlueprintRef): UUID = UUID.fromString(ref.uuid)

  implicit def uuidFromDescriptorRef(ref: DescriptorRef): UUID = UUID.fromString(ref.uuid)

  implicit def uuidFromConfigurationRef(ref: ConfigurationRef): UUID = UUID.fromString(ref.uuid)
}

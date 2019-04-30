package io.logbee.keyscore.model.configuration

import java.util.UUID
import scala.language.implicitConversions

object ToConfigurationRef {
  implicit def stringToConfigurationRef(uuid: String): ConfigurationRef = uuidToConfigurationRef(UUID.fromString(uuid))
  implicit def uuidToConfigurationRef(uuid: UUID): ConfigurationRef = ConfigurationRef(uuid.toString)
}

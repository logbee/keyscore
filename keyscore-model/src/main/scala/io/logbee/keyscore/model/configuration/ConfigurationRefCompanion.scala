package io.logbee.keyscore.model.configuration

import java.util.UUID

import scalapb.TypeMapper

trait ConfigurationRefCompanion {

  implicit def stringToDescriptorRef(uuid: String): ConfigurationRef = ConfigurationRef(uuid)

  implicit def stringToOptionDescriptorRef(uuid: String): Option[ConfigurationRef] = Option(ConfigurationRef(uuid))

  implicit def uuidToOptionDescriptorRef(uuid: UUID): ConfigurationRef = ConfigurationRef(uuid.toString)

  implicit val typeMapper = TypeMapper[String, ConfigurationRef](ConfigurationRef.apply)(_.uuid)
}

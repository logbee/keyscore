package io.logbee.keyscore.model.configuration

import scalapb.TypeMapper

trait ConfigurationRefCompanion {

  implicit def stringToDescriptorRef(uuid: String): ConfigurationRef = ConfigurationRef(uuid)

  implicit val typeMapper = TypeMapper[String, ConfigurationRef](ConfigurationRef.apply)(_.uuid)
}

package io.logbee.keyscore.model.configuration

import java.util.UUID
import java.util.UUID.randomUUID

trait ConfigurationCompanion {

  val empty = Configuration()

  def apply(parameters: Parameter*): Configuration = Configuration(randomUUID(), None, Seq(parameters:_*))
  def apply(uuid: UUID, parameters: Parameter*): Configuration = Configuration(uuid.toString, None, Seq(parameters:_*))
}

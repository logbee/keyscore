package io.logbee.keyscore.model.configuration

import java.util.UUID
import java.util.UUID.randomUUID

trait ConfigurationCompanion {

  val empty = Configuration()

  def apply(uuid: String, parameters: Parameter*): Configuration = new Configuration(ConfigurationRef(uuid), None, ParameterSet(Seq(parameters:_*)))

  def apply(uuid: UUID, parameters: Parameter*): Configuration = new Configuration(ConfigurationRef(uuid.toString), None, ParameterSet(Seq(parameters:_*)))

  def apply(parameters: Parameter*): Configuration = new Configuration(ConfigurationRef(randomUUID().toString), None, ParameterSet(Seq(parameters:_*)))
}

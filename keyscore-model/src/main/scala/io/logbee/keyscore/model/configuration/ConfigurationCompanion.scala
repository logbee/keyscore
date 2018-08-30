package io.logbee.keyscore.model.configuration

trait ConfigurationCompanion {

  def apply(parameters: Parameter*): Configuration = Configuration(None, Seq(parameters:_*))
}

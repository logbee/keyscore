package io.logbee.keyscore.model.configuration

trait ConfigurationCompanion {

  def apply(parameters: Seq[Parameter]): Configuration = Configuration(None, parameters)
}

package io.logbee.keyscore.commons.cluster.resources

import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}

object ConfigurationMessages {

    case class StoreConfigurationRequest(configuration: Configuration)

    case object StoreConfigurationResponse

    case class DeleteConfigurationRequest(ref: ConfigurationRef)

    case class GetConfigurationRequest(ref: ConfigurationRef)

    case class GetConfigurationResponse(configuration: Option[Configuration])
}

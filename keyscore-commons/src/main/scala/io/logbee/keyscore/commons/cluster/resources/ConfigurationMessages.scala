package io.logbee.keyscore.commons.cluster.resources

import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}

object ConfigurationMessages {

    case class StoreConfigurationRequest(configuration: Configuration)

    case object StoreConfigurationResponse

    case class DeleteConfigurationRequest(ref: ConfigurationRef)

    case object  DeleteConfigurationResponse

    case object DeleteAllConfigurationsRequest

    case object DeleteAllConfigurationsResponse

    case class GetConfigurationRequest(ref: ConfigurationRef)

    case object GetAllConfigurationRequest

    case class GetAllConfigurationResponse(configuations: Map[ConfigurationRef, Configuration])

    case class GetConfigurationResponse(configuration: Option[Configuration])
}

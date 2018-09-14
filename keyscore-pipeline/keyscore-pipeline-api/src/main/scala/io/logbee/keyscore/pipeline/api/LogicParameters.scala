package io.logbee.keyscore.pipeline.api

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.pipeline.api.stage.StageContext

case class LogicParameters(uuid: UUID, context: StageContext, configuration: Configuration)

package io.logbee.keyscore.pipeline.api

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.stage.StageContext

case class LogicParameters(uuid: UUID, supervisor: StageSupervisor, context: StageContext, configuration: Configuration)

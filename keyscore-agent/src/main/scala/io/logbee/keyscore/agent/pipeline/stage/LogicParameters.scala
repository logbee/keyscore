package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration

case class LogicParameters(uuid: UUID, context: StageContext, configuration: Configuration)

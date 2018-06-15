package io.logbee.keyscore.agent.pipeline.valve

import java.util.UUID

case class ValveState(id: UUID, isPaused: Boolean = false, isDrained: Boolean = false, bufferSize: Int = 0, bufferLimit: Int = 0)

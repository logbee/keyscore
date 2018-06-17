package io.logbee.keyscore.agent.pipeline.valve

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValvePosition.{Open, ValvePosition}

case class ValveState(id: UUID, position: ValvePosition = Open, bufferSize: Int = 0, bufferLimit: Int = 0, throughputTime: Long = 0, totalThroughputTime: Long = 0)

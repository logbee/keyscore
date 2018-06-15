package io.logbee.keyscore.agent.pipeline.valve

import java.util.UUID

import io.logbee.keyscore.agent.util.RingBuffer
import io.logbee.keyscore.model.Dataset

case class ValveState(uuid: UUID, isPaused: Boolean, allowDrain: Boolean = false, ringBuffer: RingBuffer[Dataset])

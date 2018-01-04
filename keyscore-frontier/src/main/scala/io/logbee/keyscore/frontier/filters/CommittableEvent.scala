package io.logbee.keyscore.frontier.filters

import java.util.UUID

import akka.kafka.ConsumerMessage
import io.logbee.keyscore.model.{Event, Field}

case class CommittableEvent(override val id: UUID, override val payload: Map[String, Field], offset: ConsumerMessage.CommittableOffset) extends Event(id, payload)

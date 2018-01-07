package io.logbee.keyscore.frontier.filters

import java.util.UUID
import java.util.UUID.randomUUID

import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.CommittableOffset
import io.logbee.keyscore.model.{Event, Field}

object CommittableEvent {
  def apply(payload: Map[String, Field], offset: CommittableOffset = null): CommittableEvent = {
    new CommittableEvent(randomUUID(), payload, offset)
  }

  def apply(event: CommittableEvent, payload: Field*): CommittableEvent = {
    new CommittableEvent(event.id, event.payload ++ payload.map(field => (field.name, field)).toMap, event.offset)
  }

  def apply(payload: Field*): CommittableEvent = {
    apply(payload.map(field => (field.name, field)).toMap)
  }

  def apply(): CommittableEvent = {
    new CommittableEvent(randomUUID(), Map.empty, null)
  }
}

case class CommittableEvent(override val id: UUID, override val payload: Map[String, Field], offset: ConsumerMessage.CommittableOffset) extends Event(id, payload)

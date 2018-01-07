package io.logbee.keyscore.model

import java.util.UUID
import java.util.UUID.randomUUID

object Event {
  def apply(payload: Map[String, Field]): Event = {
    new Event(randomUUID(), payload)
  }

  def apply(payload: Field*): Event = {
    apply(payload.map(field => (field.name, field)).toMap)
  }
}

class Event(val id: UUID, val payload: Map[String, Field]) {

  override def toString = s"Event(id=$id, payload=$payload)"
}

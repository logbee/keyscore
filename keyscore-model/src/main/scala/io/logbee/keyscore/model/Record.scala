package io.logbee.keyscore.model

import java.util.UUID
import java.util.UUID.randomUUID

object Record {
  def apply(payload: Map[String, Field]): Record = {
    new Record(randomUUID(), payload)
  }

  def apply(payload: Field*): Record = {
    apply(payload.map(field => (field.name, field)).toMap)
  }
}

class Record(val id: UUID, val payload: Map[String, Field]) {

  override def toString = s"Record(id=$id, payload=$payload)"
}

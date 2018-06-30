package io.logbee.keyscore.model

import java.util.UUID
import java.util.UUID.randomUUID

object Record {

  def apply(id: UUID, payload: Field[_]*): Record = {
    new Record(id, payload.map(field => (field.name, field)).toMap)
  }

  def apply(id: UUID, payload: List[Field[_]]): Record = {
    new Record(id, payload.map(field => (field.name, field)).toMap)
  }

  def apply(id: UUID, payload: Map[String, Field[_]]): Record = {
    new Record(id, payload)
  }

  def apply(payload: Map[String, Field[_]]): Record = {
    new Record(randomUUID(), payload)
  }

  def apply(payload: Field[_]*): Record = {
    apply(payload.map(field => (field.name, field)).toMap)
  }
}

class Record(val id: UUID, val payload: Map[String, Field[_]]) {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Record]

  override def equals(other: Any): Boolean = other match {
    case that: Record =>
      (that canEqual this) &&
        id == that.id &&
        payload == that.payload
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(id, payload)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Record(id=$id, payload=$payload)"
}

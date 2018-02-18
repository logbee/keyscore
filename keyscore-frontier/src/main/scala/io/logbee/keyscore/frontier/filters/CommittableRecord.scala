package io.logbee.keyscore.frontier.filters

import java.util.UUID
import java.util.UUID.randomUUID

import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.CommittableOffset
import io.logbee.keyscore.model.{Field, Record}

object CommittableRecord {
  def apply(payload: Map[String, Field], offset: CommittableOffset = null): CommittableRecord = {
    new CommittableRecord(randomUUID(), payload, offset)
  }

  def apply(record: CommittableRecord, payload: Field*): CommittableRecord = {
    new CommittableRecord(record.id, record.payload ++ payload.map(field => (field.name, field)).toMap, record.offset)
  }

  def apply(payload: Field*): CommittableRecord = {
    apply(payload.map(field => (field.name, field)).toMap)
  }

  def apply(): CommittableRecord = {
    new CommittableRecord(randomUUID(), Map.empty, null)
  }
}

case class CommittableRecord(override val id: UUID, override val payload: Map[String, Field], offset: ConsumerMessage.CommittableOffset) extends Record(id, payload)

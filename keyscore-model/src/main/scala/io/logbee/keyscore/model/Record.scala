package io.logbee.keyscore.model

import java.util.UUID
import java.util.UUID.randomUUID
import scala.collection.JavaConverters._
import io.logbee.keyscore.model.NativeModel.{NativeField, NativeRecord}

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

  implicit def recordToNative(record: Record): NativeRecord = {
    val builder = NativeRecord.newBuilder
    builder.setId(record.id.toString)
    record.payload.values.foreach(builder.addField(_))
    builder.build()
  }

  implicit def recordFromNative(native: NativeRecord): Record = {
    import Field.fieldFromNative
    Record(UUID.fromString(native.getId), native.getFieldList.asScala.map(fieldFromNative[Field[_]]).toList)
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

package io.logbee.keyscore.commons.replication.internal.proto

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.serialization.{SerializationExtension, SerializerWithStringManifest}
import com.google.protobuf.ByteString
import io.logbee.keyscore.commons.replication.Term
import io.logbee.keyscore.commons.replication.internal.Replicator.{AppendEntry, CommitEntry, ProtocolMessage}
import io.logbee.keyscore.commons.serialization.ActorRefSerialization

import scala.collection.immutable

class ReplicationMessageSerializer(val system: ExtendedActorSystem) extends SerializerWithStringManifest with ActorRefSerialization {

  override def identifier: Int = 270311

  private lazy val serialization = SerializationExtension(system)

  private val manifests = immutable.HashMap[Class[_], String](
    classOf[AppendEntry] -> "A",
    classOf[CommitEntry] -> "B",
  )

  private val fromBinaryMap = immutable.HashMap[String, Array[Byte] => ProtocolMessage](
    manifests(classOf[AppendEntry]) -> fromBinaryAppendEntry,
    manifests(classOf[CommitEntry]) -> fromBinaryCommitEntry,
  )

  override def manifest(obj: AnyRef): String = manifests.get(obj.getClass) match {
    case Some(manifest) => manifest
    case _ => throw new IllegalArgumentException(s"Can't serialize object of type ${obj.getClass} in [${getClass.getName}]")
  }

  override def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case message: AppendEntry => toBinaryAppendEntry(message)
    case message: CommitEntry => toBinaryCommitEntry(message)
    case _ => throw new IllegalArgumentException(s"Can't serialize object of type ${obj.getClass} in [${getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = fromBinaryMap.get(manifest) match {
    case Some(fromBinaryFunction) => fromBinaryFunction(bytes)
    case _ => throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
  }

  private def toBinaryAppendEntry(message: AppendEntry): Array[Byte] = {
    AppendEntryMessage(
      index = message.index,
      term = message.term.term,
      committedIndex  = message.committedIndex,
      previousTerm = message.previousTerm.term,
      payload = ByteString.copyFrom(message.payload),
      replyTo = serializeTypedActorRef(message.replyTo)
    ).toByteArray
  }

  private def fromBinaryAppendEntry(bytes: Array[Byte]): AppendEntry = {
    val message = AppendEntryMessage.parseFrom(bytes)
    AppendEntry(
      index = message.index,
      term = Term(message.term),
      committedIndex = message.committedIndex,
      previousTerm = Term(message.previousTerm),
      payload = message.payload.toByteArray,
      replyTo = deserializeTypedActorRef(message.replyTo)
    )
  }

  private def toBinaryCommitEntry(message: CommitEntry): Array[Byte] = {
    CommitEntryMessage(
      index = message.index,
      replyTo = serializeTypedActorRef(message.replyTo)
    ).toByteArray
  }

  private def fromBinaryCommitEntry(bytes: Array[Byte]): CommitEntry = {
    val message = CommitEntryMessage.parseFrom(bytes)
    CommitEntry(
      index = message.index,
      replyTo = deserializeTypedActorRef(message.replyTo)
    )
  }
}


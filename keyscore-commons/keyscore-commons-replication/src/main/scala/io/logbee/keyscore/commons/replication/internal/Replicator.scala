package io.logbee.keyscore.commons.replication.internal

import akka.actor.{Actor, ActorLogging, ActorSystem, typed}
import akka.serialization.{SerializationExtension, Serializers}
import com.google.protobuf.ByteString
import io.logbee.keyscore.commons.replication.Replicator.{Apply, Event, Payload, Replicate}
import io.logbee.keyscore.commons.replication.Term
import io.logbee.keyscore.commons.replication.internal.Replicator.Entry.createEntrySerializer
import io.logbee.keyscore.commons.replication.internal.Replicator.{AppendEntry, CommitEntry}

class Replicator extends Actor with ActorLogging {

  private val entrySerializer = createEntrySerializer(context.system)

  override def receive: Receive = {

    case Replicate(command, passthrough, replyTo) => log.info(s"Replicating $command")

    case Apply(command, passthrough, replayTo) =>

    case AppendEntry(index, term, committedIndex, previousTerm, bytes, replyTo) =>

      val entry = entrySerializer.deserialize(bytes)

    case CommitEntry(command, replyTo) =>
  }
}

object Replicator {

  sealed trait ProtocolMessage extends Serializable

  case class AppendEntry(index: Long, term: Term, committedIndex: Long, previousTerm: Term, payload: Array[Byte], replyTo: typed.ActorRef[Event]) extends ProtocolMessage

  case class CommitEntry(index: Long, replyTo: typed.ActorRef[Event]) extends ProtocolMessage

  case class Entry(index: Long, term: Long, payload: Payload)

  object Entry {

    trait Serializer {
      def serialize(entry: Entry): Array[Byte]
      def deserialize(entry: Array[Byte]): Entry
    }

    def createEntrySerializer(system: ActorSystem): Serializer = new Serializer {

      private val serialization = SerializationExtension(system)
      private val serializer = serialization.serializerFor(classOf[Replicator.Entry])

      override def serialize(entry: Entry): Array[Byte] = {
        proto.Entry(entry.index, entry.term, proto.Payload(
          bytes = ByteString.copyFrom(serializer.toBinary(entry.payload)),
          serializerId = serializer.identifier,
          manifest = Serializers.manifestFor(serializer, entry.payload)
        )).toByteArray
      }

      override def deserialize(bytes: Array[Byte]): Entry = {
        val entry = proto.Entry.parseFrom(bytes)
        val payload = serialization
          .deserialize(
            bytes = entry.payload.bytes.toByteArray,
            serializerId = entry.payload.serializerId,
            manifest = entry.payload.manifest
          )
          .get.asInstanceOf[Payload]

        Entry(entry.index, entry.term, payload)
      }
    }
  }
}

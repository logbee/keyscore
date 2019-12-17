package io.logbee.keyscore.commons

import java.io.Serializable

import akka.actor.typed

package object replication {

  case class Realm(realm: String)
  case class Term(term: Long)

  case class ReplicatorLookupException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  object Replicator {

    type Payload = AnyRef with Serializable
    type Passthrough = Option[AnyRef]

    sealed trait Message extends Serializable
    sealed trait Command extends Message
    sealed trait Event extends Message

    case class Replicate(command: Payload, passthrough: Passthrough, replyTo: typed.ActorRef[Event]) extends Command

    case class Apply(command: Payload, passthrough: Passthrough, replayTo: typed.ActorRef[Command]) extends Event

    case class Query(query: Payload, passthrough: Passthrough, replyTo: typed.ActorRef[Event]) extends Command
  }
}

package io.logbee.keyscore.commons.serialization

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorRef, ExtendedActorSystem, typed}
import akka.serialization.Serialization.serializedActorPath


trait ActorRefSerialization {

  val system: ExtendedActorSystem

  def serializeActorRef(actorRef: ActorRef): String =
    if (actorRef != null) serializedActorPath(actorRef)
    else ""

  def serializeActorRef(actorRef: Option[ActorRef]): String =
    actorRef.map(ref => serializedActorPath(ref)).getOrElse("")

  def serializeTypedActorRef(actorRef: typed.ActorRef[_]): String =
    if (actorRef != null) serializedActorPath(actorRef.toUntyped)
    else ""

  def serializeTypedActorRef(actorRef: Option[typed.ActorRef[_]]): String =
    actorRef.map(ref => serializedActorPath(ref.toUntyped)).getOrElse("")

  def deserializeActorRef[T](actorRef: String): ActorRef =
    if (actorRef != null && actorRef.nonEmpty) system.provider.resolveActorRef(actorRef)
    else null

  def deserializeTypedActorRef[T](actorRef: String): typed.ActorRef[T] =
    if (actorRef != null && actorRef.nonEmpty) system.provider.resolveActorRef(actorRef)
    else null
}

package io.logbee.keyscore.commons.actor

import akka.actor.typed.{ActorRefResolver, ActorSystem}
import akka.actor.{ActorRef, ExtendedActorSystem, ScalaActorRef, typed}
import akka.serialization.Serialization

import scala.language.implicitConversions

trait SerializedActorRefAdapter {

  implicit def Serialized2ScalaActorRef(arg: SerializedActorRef)(implicit system: ExtendedActorSystem): ScalaActorRef = {
    system.provider.resolveActorRef(arg.ref)
  }

  implicit def Serialized2ActorRef(arg: SerializedActorRef)(implicit system: ExtendedActorSystem): ActorRef = {
    system.provider.resolveActorRef(arg.ref)
  }

  implicit def Serialized2TypedActorRef[T](arg: SerializedActorRef)(implicit system: ActorSystem[AnyRef]): typed.ActorRef[T] = {
    ActorRefResolver(system).resolveActorRef(arg.ref)
  }

  implicit def ActorRef2Serialized(arg: ActorRef): SerializedActorRef = {
    SerializedActorRef(Serialization.serializedActorPath(arg))
  }

  implicit def TypedActorRef2Serialized(arg: typed.ActorRef[AnyRef])(implicit system: ActorSystem[AnyRef]): SerializedActorRef = {
    SerializedActorRef(ActorRefResolver(system).toSerializationFormat(arg))
  }
}

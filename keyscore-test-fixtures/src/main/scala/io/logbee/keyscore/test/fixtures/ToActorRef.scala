package io.logbee.keyscore.test.fixtures

import akka.actor.{ActorRef, typed}
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.testkit.TestProbe

import scala.language.implicitConversions

object ToActorRef {

  import akka.actor.typed.scaladsl.adapter._

  implicit def Probe2ActorRef(probe: TestProbe): ActorRef = probe.ref

  implicit def Probe2SomeActorRef(probe: TestProbe): Option[ActorRef] = Some(probe.ref)

  implicit def Probe2TypedActorRef[T](probe: TestProbe): akka.actor.typed.ActorRef[T] = probe.ref

  implicit def Probe2SomeTypedActorRef[T](probe: TestProbe): Option[akka.actor.typed.ActorRef[T]] = Some(probe.ref)

  implicit def TypedProbe2TypedActorRef[T](probe: akka.actor.testkit.typed.scaladsl.TestProbe[T]): akka.actor.typed.ActorRef[T] = probe.ref

  implicit def TypedProbe2SomeTypedActorRef[T](probe: akka.actor.testkit.typed.scaladsl.TestProbe[T]): Option[akka.actor.typed.ActorRef[T]] = Some(probe.ref)

  implicit def TestInbox2TypedActorRef[T](inbox: TestInbox[T]): typed.ActorRef[T] = inbox.ref

  implicit def TestInbox2SomeTypedActorRef[T](inbox: TestInbox[T]): Option[typed.ActorRef[T]] = Some(inbox.ref)
}

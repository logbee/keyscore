package io.logbee.keyscore.test.fixtures

import akka.actor.ActorRef
import akka.testkit.TestProbe

object ToActorRef {
  import akka.actor.typed.scaladsl.adapter._

  implicit def Probe2ActorRef(probe: TestProbe): ActorRef = probe.ref
  implicit def Probe2TypedActorRef[T](probe: TestProbe): akka.actor.typed.ActorRef[T] = probe.ref
}

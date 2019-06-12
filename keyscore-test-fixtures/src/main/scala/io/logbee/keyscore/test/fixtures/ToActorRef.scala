package io.logbee.keyscore.test.fixtures

import akka.actor.ActorRef
import akka.testkit.TestProbe

object ToActorRef {
  implicit def Probe2ActorRef(probe: TestProbe): ActorRef = probe.ref
}

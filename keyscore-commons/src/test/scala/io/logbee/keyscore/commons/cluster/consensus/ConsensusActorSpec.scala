package io.logbee.keyscore.commons.cluster.consensus

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import akka.serialization.Serialization
import akka.testkit.TestProbe
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpecLike, Matchers}


@RunWith(classOf[JUnitRunner])
class ConsensusActorSpec extends TestSystemWithMaterializerAndExecutionContext with FreeSpecLike with Matchers {

  "A ConsensusActor" - {

    val mediator = DistributedPubSub(system).mediator
    val probeA = TestProbe()
    val probeB = TestProbe()
    val probeAIdentifier = Serialization.serializedActorPath(probeA.ref)
    val probeBIdentifier = Serialization.serializedActorPath(probeB.ref)

    "should start an election and become leader" in {

      mediator ! Subscribe("consensus", probeA.ref)
      mediator ! Subscribe("consensus", probeB.ref)

      val testee = system.actorOf(ConsensusActor())
      val testeeIdentifier = Serialization.serializedActorPath(testee)

      probeA.expectMsg(RequestVote(testeeIdentifier, 1, 0))
      probeB.expectMsg(RequestVote(testeeIdentifier, 1, 0))

      testee tell(AgreeVote(), probeA.ref)

      probeA.expectMsg(IamLeader())
      probeB.expectMsg(IamLeader())
    }

    "as candidate" - {

      "should become follower when election fails because of another leader wins" in {

        mediator ! Subscribe("consensus", probeA.ref)
        mediator ! Subscribe("consensus", probeB.ref)

        val testee = system.actorOf(ConsensusActor())
        val testeeIdentifier = Serialization.serializedActorPath(testee)

        probeA.expectMsg(RequestVote(testeeIdentifier, 1, 0))
        probeB.expectMsg(RequestVote(testeeIdentifier, 1, 0))

        testee tell(IamLeader(), probeA.ref)
        testee tell(WhatAreYou(), probeA.ref)

        probeA.expectMsg(IamFollower())
      }

      "should accept candidates with newer term" in {

      }

      "should reject candidates with older term" in {

      }

      "should start a new election on split-vote" in {

      }

      "should deathwatch the accepted leader" in {

      }
    }

    "as follower " - {

      "should agree to an election for a newer term" in {

        val testee = system.actorOf(ConsensusActor())

        mediator ! Publish("consensus", RequestVote(probeAIdentifier, 1, 0))

        probeA.expectMsg(AgreeVote(1))
      }

      "should reject an election for the current term" in {

        val testee = system.actorOf(ConsensusActor())

        mediator ! Publish("consensus", RequestVote(probeAIdentifier, 0, 0))

        probeA.expectMsg(RejectVote())
      }

      "should reject an election for an older term" in {

        val testee = system.actorOf(ConsensusActor())

        mediator ! Publish("consensus", RequestVote(probeAIdentifier, -1, 0))

        probeA.expectMsg(RejectVote())
      }

      "should deathwatch the accepted leader" in {

      }
    }
  }
}

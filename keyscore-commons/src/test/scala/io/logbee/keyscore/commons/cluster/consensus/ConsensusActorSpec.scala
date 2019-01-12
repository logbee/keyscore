package io.logbee.keyscore.commons.cluster.consensus

import java.util.UUID

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import akka.serialization.Serialization
import akka.testkit.TestProbe
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterEach, FreeSpecLike, Matchers}


@RunWith(classOf[JUnitRunner])
class ConsensusActorSpec extends TestSystemWithMaterializerAndExecutionContext with FreeSpecLike with Matchers with BeforeAndAfterEach{

  trait RandomRealm {
    val realm = UUID.randomUUID().toString
  }

  "A ConsensusActor" - {

    val mediator = DistributedPubSub(system).mediator

    "should start an election and become leader" in new RandomRealm {

      val probeA = TestProbe()
      val probeB = TestProbe()

      mediator ! Subscribe(realm, probeA.ref)
      mediator ! Subscribe(realm, probeB.ref)

      val testee = system.actorOf(ConsensusActor(realm))
      val testeeIdentifier = Serialization.serializedActorPath(testee)

      probeA.expectMsg(RequestVote(testeeIdentifier, 1, 0))
      probeB.expectMsg(RequestVote(testeeIdentifier, 1, 0))

      testee tell(VoteCandidate(), probeA.ref)

      probeA.expectMsg(IamLeader())
      probeB.expectMsg(IamLeader())
    }

    "as candidate" - {

      "should become follower when election fails because of another leader wins" in new RandomRealm {

        val probeA = TestProbe()
        val probeB = TestProbe()

        mediator ! Subscribe(realm, probeA.ref)
        mediator ! Subscribe(realm, probeB.ref)

        val testee = system.actorOf(ConsensusActor(realm))
        val testeeIdentifier = Serialization.serializedActorPath(testee)

        probeA.expectMsg(RequestVote(testeeIdentifier, 1, 0))
        probeB.expectMsg(RequestVote(testeeIdentifier, 1, 0))

        testee tell(IamLeader(), probeA.ref)
        testee tell(WhatAreYou(), probeA.ref)

        probeA.expectMsg(IamFollower(term = 0, index = 0))
      }

      "should accept candidates with newer term" in new RandomRealm {

        val testee = system.actorOf(ConsensusActor(realm))
        val testeeIdentifier = Serialization.serializedActorPath(testee)

        val probeA = TestProbe()
        val probeAIdentifier = Serialization.serializedActorPath(probeA.ref)

        mediator ! Subscribe(realm, probeA.ref)

        probeA.expectMsg(RequestVote(testeeIdentifier,1))

        testee tell(RequestVote(probeAIdentifier, 2), probeA.ref)
        probeA.expectMsg(VoteCandidate())

        testee tell(WhatAreYou(), probeA.ref)

        probeA.expectMsg(IamFollower())
      }

      "should decline candidates with older term" in new RandomRealm {

        val testee = system.actorOf(ConsensusActor(realm))
        val testeeIdentifier = Serialization.serializedActorPath(testee)

        val probeA = TestProbe()
        val probeAIdentifier = Serialization.serializedActorPath(probeA.ref)

        mediator ! Subscribe(realm, probeA.ref)

        probeA.expectMsg(RequestVote(testeeIdentifier, term = 1))

        testee tell(RequestVote(probeAIdentifier, term = 0), probeA.ref)
        probeA.expectMsg(DeclineCandidate())

        testee tell(WhatAreYou(), probeA.ref)

        probeA.expectMsg(IamCandidate(term = 1))
      }

      "should start a new election on split-vote" in new RandomRealm {

        val testee = system.actorOf(ConsensusActor(realm))
        val testeeIdentifier = Serialization.serializedActorPath(testee)

        val probeA = TestProbe()
        val probeAIdentifier = Serialization.serializedActorPath(probeA.ref)

        mediator ! Subscribe(realm, probeA.ref)

        probeA.expectMsg(RequestVote(testeeIdentifier, 1))

        testee tell(RequestVote(probeAIdentifier), probeA.ref)
        probeA.expectMsg(DeclineCandidate())

        testee tell(WhatAreYou(), probeA.ref)

        probeA.expectMsg(IamCandidate(term = 1))
      }

      "should start a new election when the leader died" in new RandomRealm {

        val probeA = TestProbe()
        val probeB = TestProbe()

        mediator ! Subscribe(realm, probeA.ref)
        mediator ! Subscribe(realm, probeB.ref)

        val testee = system.actorOf(ConsensusActor(realm))
        val testeeIdentifier = Serialization.serializedActorPath(testee)

        testee tell(IamLeader(), probeA.ref)

        system.stop(probeA.ref)

        probeB.expectMsg(RequestVote(testeeIdentifier, 1))
      }
    }

    "as follower " - {

      "should agree to an election for a newer term" in new RandomRealm {

        val probeA = TestProbe()
        val probeAIdentifier = Serialization.serializedActorPath(probeA.ref)
        val testee = system.actorOf(ConsensusActor(realm, electionSchedulingEnabled = false))

        mediator ! Subscribe(realm, TestProbe().ref)
        mediator ! Publish(realm, RequestVote(probeAIdentifier, 1, 0))

        probeA.expectMsg(VoteCandidate(1))
      }

      "should reject an election for the current term" in new RandomRealm {

        val probeA = TestProbe()
        val probeAIdentifier = Serialization.serializedActorPath(probeA.ref)

        val testee = system.actorOf(ConsensusActor(realm, electionSchedulingEnabled = false))

        mediator ! Subscribe(realm, TestProbe().ref)
        mediator ! Publish(realm, RequestVote(probeAIdentifier, 0, 0))

        probeA.expectMsg(DeclineCandidate())
      }

      "should reject an election for an older term" in new RandomRealm {

        val probeA = TestProbe()
        val probeAIdentifier = Serialization.serializedActorPath(probeA.ref)

        val testee = system.actorOf(ConsensusActor(realm, electionSchedulingEnabled = false))

        mediator ! Subscribe(realm, TestProbe().ref)
        mediator ! Publish(realm, RequestVote(probeAIdentifier, -1, 0))

        probeA.expectMsg(DeclineCandidate())
      }

      "should deathwatch the accepted leader" in {

      }
    }
  }
}

package io.logbee.keyscore.commons.cluster.consensus

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.serialization.Serialization

import scala.concurrent.duration._

object ConsensusActor {
  def apply() = Props(new ConsensusActor())
}

class ConsensusActor extends Actor with ActorLogging {

  import context.{become, dispatcher}

  private val identifier = Serialization.serializedActorPath(self)

  private val mediator = DistributedPubSub(context.system).mediator
  private val scheduler = context.system.scheduler

  private val nodes = 3 // TODO: How to configure the number of nodes participating in consensus.

  override def preStart(): Unit = {

    mediator ! Subscribe("consensus", self)
    scheduler.scheduleOnce(1000 millis, self, StartElection())

    become(follower(FollowerState()))
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe("consensus", self)
  }

  override def receive: Receive = {
    case _ => log.error("Undefined behaviour!")
  }

  private def leader(state: LeaderState): Receive = {
    case WhatAreYou() => sender ! IamLeader(state.term)
    case _ =>
  }

  private def follower(state: FollowerState): Receive = {
    case StartElection() =>
      log.info("Starting Election")
      val candidateState = state.copy(term = state.term + 1)
      mediator ! Publish("consensus", RequestVote(identifier, candidateState.term, state.lastApplied))
      self ! AgreeVote()
      become(candidate(state))

    case RequestVote(candidate, term, lastIndex) =>
      val actorRef = context.actorSelection(candidate)
      if (term > state.term) {
        actorRef ! AgreeVote(term)
      }
      else {
        actorRef ! RejectVote()
      }

    case WhatAreYou() => sender ! IamFollower()
    case _ =>
  }

  private def candidate(state: FollowerState, agreedVotes: Int = 0): Receive = {
    case AgreeVote(_) if agreedVotes + 1 >= nodes / 2 + 1 =>
      log.info("Election finished. Becoming new leader.")
      become(leader(LeaderState(state.term, Option(self))), discardOld = true)
      mediator ! Publish("consensus", IamLeader())

    case AgreeVote(_) =>
      log.info(s"AgreeVote from $sender. Outstanding votes [${agreedVotes + 1}/${nodes / 2 + 1}]")
      become(candidate(state, agreedVotes + 1), discardOld = true)

    case RejectVote() =>

    case IamLeader(_) =>
      log.info("Got message from leader. Becoming follower.")
      context.become(follower(state), discardOld = true)

    case WhatAreYou() => sender ! IamCandidate
    case _ =>
  }

  case class FollowerState(term: Long = 0, leader: Option[ActorRef] = None, committedIndex: Long = 0, lastApplied: Long = 0)

  case class LeaderState(term: Long = 0, leader: Option[ActorRef] = None, nextIndex: Map[ActorRef, Long] = Map.empty, matchIndex: Map[ActorRef, Long] = Map.empty)
}

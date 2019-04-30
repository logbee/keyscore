package io.logbee.keyscore.commons.cluster.consensus

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.serialization.Serialization
import io.logbee.keyscore.commons.cluster.consensus.ConsensusActor.identifier

import scala.concurrent.duration._
import scala.language.postfixOps

object ConsensusActor {

  def apply(realm: String, electionSchedulingEnabled: Boolean = true) = Props(new ConsensusActor(realm, electionSchedulingEnabled))

  def identifier(actorRef: ActorRef): String = Serialization.serializedActorPath(actorRef)
}

class ConsensusActor(val realm: String, electionSchedulingEnabled: Boolean = true) extends Actor with ActorLogging {

  import context.{become, dispatcher, watchWith}

  private val myIdentifier = identifier(self)

  private val mediator = DistributedPubSub(context.system).mediator

  private val nodes = 3 // TODO: How to configure the number of nodes participating in consensus.
  private val initialElectionDelay = 1000 millis
  private val electionDelay = 200 millis

  override def preStart(): Unit = {
    mediator ! Subscribe(realm, self)
    maybeScheduleElection(initialElectionDelay)
    become(follower(FollowerState()))
  }

  override def postStop(): Unit = {
    log.info("Stopping")
    mediator ! Unsubscribe(realm, self)
  }

  override def receive: Receive = {
    case _ => log.error("Undefined behaviour!")
  }

  private def leader(state: LeaderState): Receive = {

    case WhatAreYou() => sender ! IamLeader(state.term, state.committedIndex)

    case message =>
      log.error(s"[Leader] Unexpected Messages: $message")
  }

  private def follower(state: FollowerState): Receive = {

    case StartElection() if state.leader.isEmpty =>
      val candidateState = state.copy(term = state.term.next)
      log.info(s"Starting Election for term: ${candidateState.term}")
      mediator ! Publish(realm, RequestVote(myIdentifier, candidateState.term, state.lastApplied))
      self ! VoteCandidate()
      become(candidate(candidateState))

    case RequestVote(candidate, term, lastIndex) =>
      val actorRef = context.actorSelection(candidate)
      if (term > state.term) {
        actorRef ! VoteCandidate(term)
      }
      else {
        actorRef ! DeclineCandidate()
      }

    case LeaderDied(leaderIdentifier, _) if state.leader.nonEmpty && identifier(state.leader.get).equals(leaderIdentifier) =>
      log.info(s"The Leader is dead. Long live the Leader. Starting election in $electionDelay.")
      become(follower(state.copy(leader = None)), discardOld = true)
      maybeScheduleElection()

    case IamLeader(term, index) =>
      log.info("Got message from leader. Staying follower.")
      become(follower(state.copy(leader = Option(sender), term = term)), discardOld = true)
      watchWith(sender, LeaderDied(identifier(sender), term))

    case WhatAreYou() => sender ! IamFollower(state.term, state.committedIndex)

    case message =>
      log.error(s"[Follower] Unexpected Messages: $message")
  }

  private def candidate(state: FollowerState, votes: Int = 0): Receive = {

    case VoteCandidate(_) if votes + 1 >= nodes / 2 + 1 =>
      log.info(s"Election finished. Becoming new leader of term ${state.term}.")
      become(leader(LeaderState(state.term, Option(self))), discardOld = true)
      mediator ! Publish(realm, IamLeader())

    case VoteCandidate(_) =>
      log.info(s"Got vote. Outstanding votes [${votes + 1}/${nodes / 2 + 1}]")
      become(candidate(state, votes + 1), discardOld = true)

    case DeclineCandidate() =>

    case IamLeader(term, index) =>
      log.info("Got message from leader. Canceling election and becoming follower again.")
      become(follower(state.copy(term = state.term.previous)), discardOld = true)
      watchWith(sender, LeaderDied(identifier(sender), term))

    case RequestVote(candidate, term, _) if !myIdentifier.equals(candidate)=>
      if(state.term > term) {
        log.info(s"Declining candidate due to higher term: ${state.term} > $term")
        sender ! DeclineCandidate()
      } else if (state.term < term) {
        log.info(s"Voting candidate due to lower term: ${state.term} < $term")
        sender ! VoteCandidate()
        become(follower(state.copy(term = state.term.previous)), discardOld = true)
      } else {
        log.error("Unhandled Split-Vote!")
      }

    case WhatAreYou() => sender ! IamCandidate(state.term, state.committedIndex)

    case message =>
      log.error(s"[Candidate] Unexpected Messages: $message")
  }

  private def maybeScheduleElection(delay: FiniteDuration = electionDelay): Unit = {
    if (electionSchedulingEnabled) {
      log.debug(s"Scheduling an election in $delay.")
      context.system.scheduler.scheduleOnce(delay, self, StartElection())
    }
  }

  case class FollowerState(term: Term = Term(), leader: Option[ActorRef] = None, committedIndex: Long = 0, lastApplied: Long = 0)

  case class LeaderState(term: Term = Term(), leader: Option[ActorRef] = None, committedIndex: Long = 0, nextIndex: Map[ActorRef, Long] = Map.empty, matchIndex: Map[ActorRef, Long] = Map.empty)
}

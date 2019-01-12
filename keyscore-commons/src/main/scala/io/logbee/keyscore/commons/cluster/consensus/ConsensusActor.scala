package io.logbee.keyscore.commons.cluster.consensus

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.serialization.Serialization

import scala.concurrent.duration._

object ConsensusActor {
  def apply(realm: String, electionSchedulingEnabled: Boolean = true) = Props(new ConsensusActor(realm, electionSchedulingEnabled))
}

class ConsensusActor(val realm: String, electionSchedulingEnabled: Boolean = true) extends Actor with ActorLogging {

  import context.{become, dispatcher, watch}

  private val identifier = Serialization.serializedActorPath(self)

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
      val candidateState = state.copy(term = state.term + 1)
      log.info(s"Starting Election for term: ${candidateState.term}")
      mediator ! Publish(realm, RequestVote(identifier, candidateState.term, state.lastApplied))
      self ! VoteCandidate()
      become(candidate(candidateState))

    case Terminated(actorRef) if state.leader.nonEmpty && state.leader.get.equals(actorRef) =>
      log.info(s"Leader is dead. Long live the Leader. Starting election in $electionDelay.")
      become(follower(state.copy(leader = None)), discardOld = true)
      maybeScheduleElection()

    case RequestVote(candidate, term, lastIndex) =>
      val actorRef = context.actorSelection(candidate)
      if (term > state.term) {
        actorRef ! VoteCandidate(term)
      }
      else {
        actorRef ! DeclineCandidate()
      }

    case IamLeader(term, index) =>
      log.info("Got message from leader. Staying follower.")
      become(follower(state.copy(leader = Option(sender), term = term)), discardOld = true)
      watch(sender)

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
      become(follower(state.copy(term = state.term - 1)), discardOld = true)
      watch(sender)

    case RequestVote(candidate, term, _) if !identifier.equals(candidate)=>
      if(state.term > term) {
        log.info(s"Declining candidate due to higher term: ${state.term} > $term")
        sender ! DeclineCandidate()
      } else if (state.term < term) {
        log.info(s"Voting candidate due to lower term: ${state.term} < $term")
        sender ! VoteCandidate()
        become(follower(state.copy(term = state.term - 1)), discardOld = true)
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

  case class FollowerState(term: Long = 0, leader: Option[ActorRef] = None, committedIndex: Long = 0, lastApplied: Long = 0)

  case class LeaderState(term: Long = 0, leader: Option[ActorRef] = None, committedIndex: Long = 0, nextIndex: Map[ActorRef, Long] = Map.empty, matchIndex: Map[ActorRef, Long] = Map.empty)
}

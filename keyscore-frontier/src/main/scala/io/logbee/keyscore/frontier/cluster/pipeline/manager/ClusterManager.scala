package io.logbee.keyscore.frontier.cluster.pipeline.manager

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.Topics._
import io.logbee.keyscore.commons.cluster.{MemberRemoved => _, _}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.ClusterAgentManager.AddAgent

import scala.concurrent.ExecutionContext

object ClusterManager {
  def apply(clusterAgentManager: ActorRef): Props = {
    Props(new ClusterManager(clusterAgentManager))
  }
}

class ClusterManager(aM: ActorRef) extends Actor with ActorLogging {
  private implicit val ec: ExecutionContext = context.dispatcher

  private val system = context.system
  private val cluster = Cluster(context.system)
  private val mediator = DistributedPubSub(system).mediator
  private val scheduler = context.system.scheduler

  private val clusterAgentManager = aM

  override def preStart(): Unit = {
    log.info("ClusterManager started.")
    cluster.subscribe(self, classOf[MemberJoined])
    cluster.subscribe(self, classOf[MemberUp])

    cluster.subscribe(self, classOf[ReachableMember])
    cluster.subscribe(self, classOf[UnreachableMember])

    cluster.subscribe(self, classOf[MemberLeft])
    cluster.subscribe(self, classOf[MemberExited])
    cluster.subscribe(self, classOf[MemberRemoved])

    cluster.subscribe(self, classOf[LeaderChanged])

    mediator ! Subscribe(ClusterTopic, self)
    mediator ! Publish(ClusterTopic, MemberJoin("ClusterManager", cluster.selfMember))
  }

  override def postStop(): Unit = {
    mediator ! Publish(ClusterTopic, MemberLeave("ClusterManager", cluster.selfMember))
    cluster.unsubscribe(self)
    mediator ! Unsubscribe(ClusterTopic, self)
    log.info("ClusterManager stopped.")
  }

  override def receive: Receive = {
    case MemberJoined(member) =>
      log.info(s"[Cluster] Member joined: ${member.uniqueAddress} with roles ${member.roles}")

    case MemberUp(member) =>
      log.info(s"[Cluster] Member is up: ${member.uniqueAddress} with roles ${member.roles}")
      if (member.hasRole("keyscore-agent")) {
        clusterAgentManager ! AddAgent(member)
      }

    case ReachableMember(member) =>
      log.info(s"[Cluster] Member is reachable: ${member.uniqueAddress} with roles ${member.roles}")
      if (member.hasRole("keyscore-agent")) {
        clusterAgentManager ! AddAgent(member)
      }

    case UnreachableMember(member) =>
      log.info(s"[Cluster] Member is unreachable: ${member.uniqueAddress} with roles ${member.roles}")

    case MemberLeft(member) =>
      log.info(s"[Cluster] Member left: ${member.uniqueAddress} with roles ${member.roles}")

    case MemberExited(member) =>
      log.info(s"[Cluster] Member exited: ${member.uniqueAddress} with roles ${member.roles}")

    case MemberRemoved(member, previousStatus) =>
      log.info(s"[Cluster] Member is up: ${member.uniqueAddress} with previous status ${previousStatus} with roles ${member.roles}")

    case LeaderChanged(leader) =>
      log.info(s"[Cluster] Leader changed: ${leader.get}")

    case MemberJoin(name, member) =>
      log.info(s"[Mediator] Member [${name}] is ${member.status}: ${member.uniqueAddress} | ${member.roles}")

    case MemberLeave(name, member) =>
      log.info(s"[Mediator] Member [${name}] is ${member.status}: ${member.uniqueAddress} | ${member.roles}")

    case ActorJoin(name, actor) =>
      log.info(s"[Mediator] Member [${name}] started: ${actor}")

    case ActorLeave(name, actor) =>
      log.info(s"[Mediator] Member [${name}] stopped: ${actor}")

  }
}

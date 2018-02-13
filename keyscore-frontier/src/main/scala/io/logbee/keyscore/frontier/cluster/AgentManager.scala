package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Address}
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck, Unsubscribe}
import akka.cluster.{Cluster, Member}
import io.logbee.keyscore.cluster.AgentJoin
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse}

import scala.collection.mutable


object AgentManager {
  case object QueryAgents
  case class QueryAgentsResponse(agents: List[RemoteAgent])
}

class AgentManager extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val agents: mutable.HashMap[Long, RemoteAgent] = mutable.HashMap.empty
  val members: mutable.HashMap[Address, Long] = mutable.HashMap.empty
  val mediator: ActorRef  = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
    cluster.subscribe(self, classOf[UnreachableMember])
    cluster.subscribe(self, classOf[ReachableMember])
    cluster.subscribe(self, classOf[MemberExited])

    mediator ! Subscribe("agents", self)
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    mediator ! Unsubscribe("agents", self)
  }

  override def receive: Receive = {
    case SubscribeAck(Subscribe("agents", None, `self`)) =>
      log.info("subscribing")

    case AgentJoin(name) =>
      val uid = members(sender().path.address)
      val agent = RemoteAgent(uid, name)
      agents += (uid -> agent)
      log.info(s"Agent joined: $agent")

    case MemberExited(member) =>
      removeAgent(member)

    case ReachableMember(member) =>
      addAgentMember(member)

    case MemberUp(member) =>
      addAgentMember(member)

    case UnreachableMember(member) =>
      removeAgent(member)

    case QueryAgents =>
      sender() ! QueryAgentsResponse(agents.values.toList)
  }

  private def addAgentMember(member: Member): Unit = {
    if (member.hasRole("keyscore-agent")) {
      members += (member.uniqueAddress.address -> member.uniqueAddress.longUid)
    }
  }

  private def removeAgent(member: Member): Unit = {

    val uid = member.uniqueAddress.longUid
    val address = member.uniqueAddress.address

    agents.get(uid) match {
      case Some(agent) =>
        agents.remove(uid)
        members.remove(address)
        log.info(s"Removed Agent: $agent")
    }
  }
}

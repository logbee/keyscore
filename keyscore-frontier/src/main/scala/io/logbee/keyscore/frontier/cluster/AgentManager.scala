package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Address}
import akka.cluster.ClusterEvent
import akka.cluster.ClusterEvent.{MemberExited, MemberUp, ReachableMember, UnreachableMember}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck, Unsubscribe}
import akka.cluster.{Cluster, Member, UniqueAddress}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse, QueryMembers, QueryMembersAddresses}

import scala.collection.mutable


object AgentManager {
  case object QueryAgents
  case object QueryMembers
  case class QueryAgentsResponse(agents: List[RemoteAgent])
  case class QueryMembers(members: List[Member])
  case class QueryMembersAddresses(addresses: List[UniqueAddress])
}

class AgentManager extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val agents: mutable.HashMap[Long, RemoteAgent] = mutable.HashMap.empty
  val members: mutable.HashMap[Long, Member] = mutable.HashMap.empty
  val mediator: ActorRef  = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
    cluster.subscribe(self, classOf[ReachableMember])
    cluster.subscribe(self, classOf[UnreachableMember])
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
      val agent = RemoteAgent(name, 0, sender)
      agents += (agent.memberId -> agent)
      sender ! AgentJoinAccepted()
      mediator ! Publish("agents", AgentJoined(agent.ref))

    case MemberExited(member) =>
      removeAgent(member)

    case ReachableMember(member) =>
      addAgentMember(member)

    case MemberUp(member) =>
      addAgentMember(member)
      sender ! MemberAdded(member)

    case UnreachableMember(member) =>
      removeAgent(member)
      sender ! MemberRemoved(member)

    case QueryAgents =>
      sender ! QueryAgentsResponse(agents.values.toList)

    case QueryMembers =>
      sender ! QueryMembers(members.values.toList)
  }

  private def addAgentMember(member: Member): Unit = {
    if (member.hasRole("keyscore-agent")) {
      members += (member.uniqueAddress.longUid -> member)
    }
  }

  private def removeAgent(member: Member): Unit = {

    val uid = member.uniqueAddress.longUid
    val address = member.uniqueAddress.address

    agents.get(uid) match {
      case Some(agent) =>
        agents.remove(uid)
        members.remove(uid)
        mediator ! Publish("agents", AgentLeaved(agent.ref))
    }
  }
}

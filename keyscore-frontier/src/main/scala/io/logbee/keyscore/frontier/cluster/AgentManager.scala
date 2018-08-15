package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import akka.cluster.ClusterEvent.{MemberExited, MemberUp, ReachableMember, UnreachableMember}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck, Unsubscribe}
import akka.cluster.{Cluster, Member, UniqueAddress}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse, QueryMembers}

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
  val members: mutable.ListBuffer[Member] = mutable.ListBuffer.empty
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

    case AgentJoin(id, name) =>
      members.find(member => sender.path.address.equals(member.address)) match {
        case Some(member) =>
          val agent = RemoteAgent(id, name, member.uniqueAddress.longUid, sender)
          agents += (agent.memberId -> agent)
          sender ! AgentJoinAccepted()
          mediator ! Publish("agents", AgentJoined(agent.ref))

          log.info(s"Member joint as Agent: $agent")

        case _ =>
          sender ! AgentJoinFailure(73)
      }

    case MemberExited(member) =>
      log.info("Member is exited: "+member.uniqueAddress)
      removeAgent(member)

    case ReachableMember(member) =>
      log.info("Member is reachable: "+member.uniqueAddress)
      addAgentMember(member)

    case MemberUp(member) =>
      log.info("Member is up: "+member.uniqueAddress)
      addAgentMember(member)
      sender ! MemberAdded(member)

    case UnreachableMember(member) =>
      log.info("Member is unreachable: "+member.uniqueAddress)
      removeAgent(member)
      sender ! MemberRemoved(member)

    case QueryAgents =>
      sender ! QueryAgentsResponse(agents.values.toList)

    case QueryMembers =>
      sender ! QueryMembers(members.toList)

    case RemoveAgentFromCluster(agentID) =>
      agents.find(agent => agent._2.id.equals(agentID)) match {
        case Some((memberID, remoteAgent)) =>
          members.find(member => member.uniqueAddress.longUid.equals(memberID)) match {
            case Some(member) =>
              stopAgent(member, sender(), remoteAgent)
            case _ =>
              sender ! RemoveAgentFromClusterFailed
          }
        case _ =>
          sender ! RemoveAgentFromClusterFailed
      }
  }

  private def addAgentMember(member: Member): Unit = {
    if (member.hasRole("keyscore-agent")) {
      members += member
    }
  }

  private def removeAgent(member: Member): Unit = {
    val uid = member.uniqueAddress.longUid

    agents.get(uid) match {
      case Some(agent) =>
        log.info("Agent Leaved: " + agent.id)
        agents.remove(uid)
        members.remove(members.indexWhere(member => uid == member.uniqueAddress.longUid))
        mediator ! Publish("agents", AgentLeaved(agent.ref))
      case _ =>
        log.info(s"Agent could not be removed: $uid")
    }
  }

  private def stopAgent(member: Member, senderRef: ActorRef, agent: RemoteAgent): Unit = {
    //mark the agent as down
    cluster.down(member.uniqueAddress.address)
    //stop the agent actor
    agent.ref ! PoisonPill
    log.info("Agent Stopped: " + agent.id)
    removeAgent(member)
    senderRef ! AgentRemovedFromCluster(agent.id)
  }

}



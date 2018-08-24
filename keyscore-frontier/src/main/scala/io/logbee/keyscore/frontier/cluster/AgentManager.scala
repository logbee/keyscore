package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck, Unsubscribe}
import akka.cluster.{Cluster, Member, UniqueAddress}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.frontier.cluster.AgentManager._

import scala.collection.mutable

object AgentManager {

  case object QueryAgents

  case object QueryMembers

  case class QueryAgentsResponse(agents: List[RemoteAgent])

  case class QueryMembers(members: List[Member])

  case class QueryMembersAddresses(addresses: List[UniqueAddress])

  case class AddAgent(member: Member)

  case class RemoveAgent(member: Member)

}

class AgentManager extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  val idToAgent: mutable.HashMap[Long, RemoteAgent] = mutable.HashMap.empty
  val agents: mutable.ListBuffer[Member] = mutable.ListBuffer.empty

  override def preStart(): Unit = {
    log.info("AgentManager started.")
    mediator ! Subscribe("agents", self)
    mediator ! Subscribe("cluster", self)
    mediator ! Publish("cluster", MemberJoin("AgentManager", cluster.selfMember))
  }

  override def postStop(): Unit = {
    mediator ! Publish("cluster", MemberLeave("AgentManager", cluster.selfMember))
    cluster.unsubscribe(self)
    mediator ! Unsubscribe("agents", self)
    mediator ! Unsubscribe("cluster", self)
    log.info("AgentManager stopped.")
  }

  override def receive: Receive = {
    case SubscribeAck(Subscribe("agents", None, `self`)) =>
      log.info("Subscribed to topic [agents]")

    case AgentJoin(id, name) =>
      log.info(s"Member trys to join as Agent(${id} | ${name})...")
      agents.find(member => sender.path.address.equals(member.address)) match {
        case Some(member) =>
          val agent = RemoteAgent(id, name, member.uniqueAddress.longUid, sender)
          idToAgent += (agent.memberId -> agent)
          sender ! AgentJoinAccepted()
          mediator ! Publish("agents", AgentJoined(agent.ref))

          log.info(s"Member joint as Agent: $agent")

        case _ =>
          log.info(s"Member could not join as Agent.")
          sender ! AgentJoinFailure(73)
      }

    case RemoveAgent(member) =>
      removeAgent(member)

    case AddAgent(member) =>
      addAgentMember(member)

    case QueryAgents =>
      sender ! QueryAgentsResponse(idToAgent.values.toList)

    case QueryMembers =>
      sender ! QueryMembers(agents.toList)

    case RemoveAgentFromCluster(agentID) =>
      log.info(s"Manually removing Agent with id ${agentID} from cluster.")
      idToAgent.find(agent => agent._2.id.equals(agentID)) match {
        case Some((memberID, remoteAgent)) =>
          agents.find(member => member.uniqueAddress.longUid.equals(memberID)) match {
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
    log.info(s"Agent ${member.uniqueAddress} added.")
    agents += member
  }

  private def removeAgent(member: Member): Unit = {
    val uid = member.uniqueAddress.longUid

    idToAgent.get(uid) match {
      case Some(agent) =>
        log.info(s"Agent ${member.uniqueAddress} removed.")
        idToAgent.remove(uid)
        agents.remove(agents.indexWhere(member => uid == member.uniqueAddress.longUid))
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



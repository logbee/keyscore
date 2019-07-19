package io.logbee.keyscore.frontier.cluster.pipeline.managers

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck, Unsubscribe}
import akka.cluster.{Cluster, Member, UniqueAddress}
import io.logbee.keyscore.commons.cluster.Topics._
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.frontier.cluster.RemoteAgent
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterAgentManager._

import scala.collection.mutable

/**
  * The '''ClusterAgentManager''' manages all the members with the role ''Agent'' in the cluster and <br>
  * starts the `AgentStatsManager` and the `AgentCapabilitiesManager`.<br>
  *
  * @todo Handle Operating / Sleeping
  */
object ClusterAgentManager {

  case object QueryAgents

  case object QueryMembers

  case class QueryAgentsResponse(agents: List[RemoteAgent])

  case class QueryMembersResponse(members: List[Member])

  case class QueryMembersAddresses(addresses: List[UniqueAddress])

  case class AddAgent(member: Member)

  case class RemoveAgent(member: Member)

  case class Init(isOperating: Boolean)

  case class ClusterAgentManagerInitialized(isOperating: Boolean)

  private case object ReInit
  private case object Unsubscribe

}

class ClusterAgentManager extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  val idToAgent: mutable.HashMap[Long, RemoteAgent] = mutable.HashMap.empty
  val agents: mutable.ListBuffer[Member] = mutable.ListBuffer.empty

  val agentStatsManager = context.actorOf(AgentStatsManager())
  val agentCapabilitiesManager = context.actorOf(AgentCapabilitiesManager())

  override def preStart(): Unit = {
    log.info(" started.")
  }

  override def postStop(): Unit = {
    self ! Unsubscribe
    log.info(" stopped.")
  }

  override def receive: Receive = {
    case Init(isOperating) =>
      log.debug("Initializing ClusterAgentManager ...")
      if(isOperating) {
        log.debug("Become [working]")
        context.become(working)
      } else {
        log.debug("Become [sleeping]")
        context.become(sleeping)
      }

      sender ! ClusterAgentManagerInitialized(isOperating)
      self ! ReInit
  }

  def working: Receive = {
    case ReInit =>
      mediator ! Subscribe(AgentsTopic, self)
      mediator ! Subscribe(ClusterTopic, self)
      mediator ! Publish(ClusterTopic, MemberJoin(Roles.ClusterAgentManager, cluster.selfMember))

    case SubscribeAck(Subscribe(AgentsTopic, None, `self`)) =>
      log.debug(s"Subscribed to topic [${AgentsTopic}]")

    case SubscribeAck(Subscribe(ClusterTopic, None, `self`)) =>
      log.debug(s"Subscribed to topic [${AgentsTopic}]")

    case AgentJoin(id, name) =>
      log.info(s"Member is trying to join as Agent(<${id}> | ${name})...")
      agents.find(member => sender.path.address.equals(member.address)) match {
        case Some(member) =>
          val agent = RemoteAgent(id, name, member.uniqueAddress.longUid, sender)
          idToAgent += (agent.memberId -> agent)
          sender ! AgentJoinAccepted()
          mediator ! Publish(AgentsTopic, AgentJoined(agent.ref))
          log.info(s"Member joint as Agent: $agent")

        case _ =>
          log.info(s"Member could not join as Agent.")
          sender ! AgentJoinFailure(73)
      }

    case RemoveAgent(member) =>
      log.debug(s"Removing agent <${member.uniqueAddress.longUid}>")
      removeAgent(member)

    case AddAgent(member) =>
      log.debug(s"Adding agent <${member.uniqueAddress.longUid}>")
      addAgentMember(member)

    case QueryAgents =>
      log.debug("Responding RemoteAgents")
      sender ! QueryAgentsResponse(idToAgent.values.toList)

    case QueryMembers =>
      log.debug("Responding Members")
      sender ! QueryMembersResponse(agents.toList)

    case RemoveAgentFromCluster(agentID) =>
      log.debug(s"Removing Agent($agentID) from cluster...")
      idToAgent.find(agent => agent._2.id.equals(agentID)) match {
        case Some((memberID, remoteAgent)) =>
          agents.find(member => member.uniqueAddress.longUid.equals(memberID)) match {
            case Some(member) =>
              log.debug(s"Stopping Agent($agentID)")
              stopAgent(member, sender(), remoteAgent)
            case _ =>
              log.warning(s"Couldn't find Agent($agentID) to remove from cluster.")
              sender ! RemoveAgentFromClusterFailed
          }
        case _ =>
          log.error(s"Couldn't remove Agent($agentID)")
          sender ! RemoveAgentFromClusterFailed
      }

    case Unsubscribe =>
      mediator ! Publish(ClusterTopic , MemberLeave(Roles.ClusterAgentManager, cluster.selfMember))
      mediator ! Unsubscribe(ClusterTopic, self)
      mediator ! Unsubscribe(AgentsTopic, self)
      cluster.unsubscribe(self)

    case AgentCapabilities => // TODO: ClusterAgentManager is not interested in this kind of message.
  }

  def sleeping: Receive = {
    case ReInit =>
      mediator ! Subscribe(ClusterTopic, self)
      mediator ! Publish(ClusterTopic, MemberJoin(Roles.ClusterAgentManager, cluster.selfMember))

    case SubscribeAck(Subscribe(ClusterTopic, None, `self`)) =>
      log.debug(s"Subscribed to topic [$ClusterTopic]")

    case Unsubscribe =>
      mediator ! Publish(ClusterTopic, MemberLeave(Roles.ClusterAgentManager, cluster.selfMember))
      mediator ! Unsubscribe(ClusterTopic, self)
      cluster.unsubscribe(self)

  }

  private def addAgentMember(member: Member): Unit = {
    log.debug(s"Agent ${member.uniqueAddress} added.")
    agents += member
  }

  private def removeAgent(member: Member): Unit = {
    val uid = member.uniqueAddress.longUid
    log.debug(s"Removing Agent($uid) from cluster.")
    idToAgent.get(uid) match {
      case Some(agent) =>
        log.debug(s"Agent ${member.uniqueAddress} removed.")
        idToAgent.remove(uid)
        agents.remove(agents.indexWhere(member => uid == member.uniqueAddress.longUid))
        mediator ! Publish(AgentsTopic, AgentLeaved(agent.ref))
      case _ =>
        log.debug(s"Agent could not be removed: <$uid>")
    }
  }

  private def stopAgent(member: Member, senderRef: ActorRef, agent: RemoteAgent): Unit = {
    cluster.down(member.uniqueAddress.address)
    log.warning(s"Agent [${agent.name}] stopped: <${agent.id}>")
    removeAgent(member)
    senderRef ! AgentRemovedFromCluster(agent.id)
  }

}



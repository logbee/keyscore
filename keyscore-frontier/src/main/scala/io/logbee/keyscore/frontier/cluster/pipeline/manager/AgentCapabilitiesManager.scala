package io.logbee.keyscore.frontier.cluster.pipeline.manager

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.Topics.{AgentsTopic, ClusterTopic, WhoIsTopic}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages.StoreDescriptorRequest
import io.logbee.keyscore.commons.{AgentCapabilitiesService, DescriptorService, HereIam, WhoIs}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentCapabilitiesManager._
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * The AgentCapabilitiesManager holds the capabilities of all agents in the cluster <br>
  * and returns all the possible agents for a specific set of descriptors.
  */
object AgentCapabilitiesManager {
  def apply(): Props = Props(new AgentCapabilitiesManager())

  case object GetDescriptors

  case class GetDescriptorsResponse(listOfDescriptorsAndType: List[Descriptor])

  case object GetActiveDescriptors

  case class ActiveDescriptors(listOfDescriptors: List[Descriptor])

  case class AgentsForPipelineRequest(descriptorRefs: List[DescriptorRef])

  case class AgentsForPipelineResponse(possibleAgents: List[ActorRef])

}

class AgentCapabilitiesManager extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  private var descriptorManager: ActorRef = _
  private val descriptorToActorPaths = mutable.Map.empty[Descriptor, mutable.Set[ActorPath]]
  var availableAgents: mutable.Map[ActorRef, Seq[Descriptor]] = mutable.Map.empty[ActorRef, Seq[Descriptor]]

  override def preStart(): Unit = {
    mediator ! Subscribe(AgentsTopic, self)
    mediator ! Subscribe(ClusterTopic, self)
    mediator ! Subscribe(WhoIsTopic, self)
    mediator ! Publish(ClusterTopic, ActorJoin("ClusterCapManager", self))
    mediator ! WhoIs(DescriptorService)
    log.info("AgentCapabilitiesManager started.")
  }

  override def postStop(): Unit = {
    mediator ! Publish(ClusterTopic, ActorLeave("ClusterCapManager", self))
    mediator ! Unsubscribe(AgentsTopic, self)
    mediator ! Unsubscribe(ClusterTopic, self)
    log.info("AgentCapabilitiesManager stopped.")
  }

  override def receive: Receive = {
    case HereIam(DescriptorService, ref) =>
      descriptorManager = ref
      context.become(running)
  }

  private def running: Receive = {
    case WhoIs(AgentCapabilitiesService) =>
      HereIam(AgentCapabilitiesService,self)

    case GetDescriptors =>
      sender ! GetDescriptorsResponse(descriptorToActorPaths.keys.toList)

    case AgentCapabilities(descriptors) =>
      availableAgents.getOrElseUpdate(sender, descriptors)
      descriptors.foreach(descriptor => {
        descriptorToActorPaths.getOrElseUpdate(descriptor, mutable.Set.empty) += sender.path
        descriptorManager ! StoreDescriptorRequest(descriptor)
      })

    case AgentLeaved(ref) =>
      availableAgents.remove(ref)
      descriptorToActorPaths.retain((_, paths) => {
        paths.retain(path => ref.path.address != path.address)
        paths.nonEmpty
      })

    case AgentsForPipelineRequest(descriptorRefs) =>
      val possibleAgents = createListOfPossibleAgents(descriptorRefs)
      sender ! AgentsForPipelineResponse(possibleAgents)

  }

  def checkIfCapabilitiesMatchRequirements(descriptorRefs: List[DescriptorRef], agent: (ActorRef, Seq[Descriptor])): Boolean = {

    if (descriptorRefs.count(descriptorRef => agent._2.map(descriptor => descriptor.ref).contains(descriptorRef)) ==
      descriptorRefs.size) {
      return true
    }
    false
  }

  def createListOfPossibleAgents(descriptorRefs: List[DescriptorRef]): List[ActorRef] = {

    var possibleAgents: ListBuffer[ActorRef] = ListBuffer.empty
    availableAgents.foreach { agent =>
      if (checkIfCapabilitiesMatchRequirements(descriptorRefs, agent)) {
        possibleAgents += agent._1
      } else {
        log.info(s"Agent '$agent' doesn't match requirements.")
      }
    }
    possibleAgents.toList
  }




}

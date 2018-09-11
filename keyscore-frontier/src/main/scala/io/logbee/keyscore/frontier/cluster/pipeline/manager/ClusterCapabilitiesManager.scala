package io.logbee.keyscore.frontier.cluster.pipeline.manager

import java.util.Locale

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages.StoreDescriptorRequest
import io.logbee.keyscore.commons.{DescriptorService, HereIam, WhoIs}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentManager.{AgentsForPipelineRequest, AgentsForPipelineResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.ClusterCapabilitiesManager.{ActiveDescriptors, GetActiveDescriptors, GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object ClusterCapabilitiesManager {
  def apply(): Props = Props(new ClusterCapabilitiesManager())

  case class GetStandardDescriptors(language: Locale)

  case class StandardDescriptors(listOfDescriptorsAndType: List[Descriptor])

  case object GetActiveDescriptors

  case class ActiveDescriptors(listOfDescriptors: List[Descriptor])

}

class ClusterCapabilitiesManager extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  private var descriptorManager: ActorRef = _
  private val listOfFilterDescriptors = mutable.Map.empty[Descriptor, mutable.Set[ActorPath]]
  private val listOfActiveDescriptors = List[Descriptor]()
  var availableAgents: mutable.Map[ActorRef, Seq[Descriptor]] = mutable.Map.empty[ActorRef, Seq[Descriptor]]

  override def preStart(): Unit = {
    mediator ! Subscribe("agents", self)
    mediator ! Subscribe("cluster", self)
    mediator ! Publish("cluster", ActorJoin("ClusterCapManager", self))
    mediator ! WhoIs(DescriptorService)
    log.info("ClusterCapabilitiesManager started.")
  }

  override def postStop(): Unit = {
    mediator ! Publish("cluster", ActorLeave("ClusterCapManager", self))
    mediator ! Unsubscribe("agents", self)
    mediator ! Unsubscribe("cluster", self)
    log.info("ClusterCapabilitiesManager stopped.")
  }

  override def receive: Receive = {
    case HereIam(DescriptorService, ref) =>
      descriptorManager = ref
      context.become(running)
  }

  private def running: Receive = {
    case GetStandardDescriptors(selectedLanguage) =>
      sender ! StandardDescriptors(listOfFilterDescriptors.keys.toList)

    case GetActiveDescriptors =>
      sender() ! ActiveDescriptors(listOfActiveDescriptors)

    case AgentCapabilities(descriptors) =>
      availableAgents.getOrElseUpdate(sender, descriptors)
      descriptors.foreach(descriptor => {
        listOfFilterDescriptors.getOrElseUpdate(descriptor, mutable.Set.empty) += sender.path
        descriptorManager ! StoreDescriptorRequest(descriptor)
      })

    case AgentLeaved(ref) =>
      availableAgents.remove(ref)
      listOfFilterDescriptors.retain((_, paths) => {
        paths.retain(path => ref.path.address != path.address)
        paths.nonEmpty
      })

    case AgentsForPipelineRequest(receiver, descriptorRefs) =>
      val possibleAgents = createListOfPossibleAgents(descriptorRefs)
      sender ! AgentsForPipelineResponse(receiver, possibleAgents)

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

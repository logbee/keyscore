package io.logbee.keyscore.frontier.cluster.pipeline.managers

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.Topics.{AgentsTopic, ClusterTopic, WhoIsTopic}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages.StoreDescriptorRequest
import io.logbee.keyscore.commons.util.ServiceDiscovery.discover
import io.logbee.keyscore.commons.{AgentCapabilitiesService, DescriptorService, HereIam, WhoIs}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.AgentCapabilitiesManager._
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

/**
  * The '''AgentCapabilitiesManager''' holds the `Capabilities` (Descriptors) of all agents in the cluster <br>
  * and returns all the possible agents for a specific set of descriptors if requested.
  *
  * @todo Error Handling
  */
object AgentCapabilitiesManager {
  def apply(): Props = Props(new AgentCapabilitiesManager())

  case object GetDescriptors

  case class GetDescriptorsResponse(listOfDescriptorsAndType: List[Descriptor])

  case object GetActiveDescriptors

  case class ActiveDescriptors(listOfDescriptors: List[Descriptor])

  case class AgentsForPipelineRequest(descriptorRefs: List[DescriptorRef])

  case class AgentsForPipelineResponse(possibleAgents: List[ActorRef])

  private case object InitACM

}

class AgentCapabilitiesManager extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator
  private implicit val ec = context.dispatcher

  private var descriptorManager: ActorRef = _
  private val descriptorToActorPaths = mutable.Map.empty[Descriptor, mutable.Set[ActorPath]]
  var availableAgents: mutable.Map[ActorRef, Seq[Descriptor]] = mutable.Map.empty[ActorRef, Seq[Descriptor]]

  override def preStart(): Unit = {
    mediator ! Subscribe(AgentsTopic, self)
    mediator ! Subscribe(ClusterTopic, self)
    mediator ! Subscribe(WhoIsTopic, self)
    mediator ! Publish(ClusterTopic, ActorJoin(Roles.ClusterCapabilitiesManager, self))
    self ! InitACM
    log.info(" started.")
  }

  override def postStop(): Unit = {
    mediator ! Publish(ClusterTopic, ActorLeave(Roles.ClusterCapabilitiesManager, self))
    mediator ! Unsubscribe(AgentsTopic, self)
    mediator ! Unsubscribe(ClusterTopic, self)
    log.info(" stopped.")
  }

  override def receive: Receive = {
    case InitACM =>
      discover(Seq(DescriptorService)).onComplete {
        case Success(services) =>
          descriptorManager = services(DescriptorService)
          context.become(running)

        case Failure(exception) =>
          log.error(exception, "Couldn't retrieve services.")
        // TODO: Handle discover errors!
      }
  }

  private def running: Receive = {

    case WhoIs(AgentCapabilitiesService) =>
      sender ! HereIam(AgentCapabilitiesService, self)

    case GetDescriptors =>
      log.debug("Responding list of Descriptors")
      sender ! GetDescriptorsResponse(descriptorToActorPaths.keys.toList)

    case AgentCapabilities(descriptors) =>
      availableAgents.getOrElseUpdate(sender, descriptors)
      descriptors.foreach(descriptor => {
        descriptorToActorPaths.getOrElseUpdate(descriptor, mutable.Set.empty) += sender.path
        descriptorManager ! StoreDescriptorRequest(descriptor)
      })

    case AgentLeaved(ref) =>
      log.debug(s"Agent($ref) leaved.")
      availableAgents.remove(ref)
      descriptorToActorPaths.retain((_, paths) => {
        paths.retain(path => ref.path.address != path.address)
        paths.nonEmpty
      })

    case AgentsForPipelineRequest(descriptorRefs) =>
      log.debug(s"Received AgentsForPipelineRequest with $descriptorRefs")
      val possibleAgents = createListOfPossibleAgents(descriptorRefs)
      sender ! AgentsForPipelineResponse(possibleAgents)

    case WhoIs(_) => // Nothing to do.
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
      }
    }
    possibleAgents.toList
  }
}

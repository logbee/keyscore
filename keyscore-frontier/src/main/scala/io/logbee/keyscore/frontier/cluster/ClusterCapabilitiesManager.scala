package io.logbee.keyscore.frontier.cluster

import java.util.Locale

import akka.actor.{Actor, ActorLogging, ActorPath, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.{AgentCapabilities, AgentLeaved}
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{ActiveDescriptors, GetActiveDescriptors, GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.model.descriptor.Descriptor

import scala.collection.mutable


object ClusterCapabilitiesManager {
  def props(): Props = Props(new ClusterCapabilitiesManager())

  case class GetStandardDescriptors(language: Locale)

  case class StandardDescriptors(listOfDescriptorsAndType: List[Descriptor])

  case object GetActiveDescriptors

  case class ActiveDescriptors(listOfDescriptors: List[Descriptor])

}

class ClusterCapabilitiesManager extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  private val listOfFilterDescriptors = mutable.Map.empty[Descriptor, mutable.Set[ActorPath]]
  private val listOfActiveDescriptors = List[Descriptor]()

  override def preStart(): Unit = {
    mediator ! Subscribe("agents", self)
    log.info("StartUp complete")
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe("agents", self)
    log.info("Shutdown complete")
  }

  override def receive: Receive = {
    case GetStandardDescriptors(selectedLanguage) =>
      sender ! StandardDescriptors(listOfFilterDescriptors.keys.toList)

    case GetActiveDescriptors =>
      sender() ! ActiveDescriptors(listOfActiveDescriptors)

    case AgentCapabilities(filterDescriptors) =>
      filterDescriptors.foreach(descriptors => {
        listOfFilterDescriptors.getOrElseUpdate(descriptors, mutable.Set.empty) += sender.path
      })

    case AgentLeaved(ref) =>
      listOfFilterDescriptors.retain((_, paths) => {
        paths.retain(path => ref.path.address != path.address)
        paths.nonEmpty
      })
  }
}

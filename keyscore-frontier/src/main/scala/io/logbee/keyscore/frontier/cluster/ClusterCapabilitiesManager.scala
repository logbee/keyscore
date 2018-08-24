package io.logbee.keyscore.frontier.cluster

import java.util.Locale

import akka.actor.{Actor, ActorLogging, ActorPath, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{ActiveDescriptors, GetActiveDescriptors, GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.model.filter.{FilterDescriptor, MetaFilterDescriptor}

import scala.collection.mutable


object ClusterCapabilitiesManager {
  def props(): Props = Props(new ClusterCapabilitiesManager())

  case class GetStandardDescriptors(language: Locale)

  case class StandardDescriptors(listOfDescriptorsAndType: List[FilterDescriptor])

  case object GetActiveDescriptors

  case class ActiveDescriptors(listOfDescriptors: List[FilterDescriptor])

}

class ClusterCapabilitiesManager extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  private val listOfFilterDescriptors = mutable.Map.empty[MetaFilterDescriptor, mutable.Set[ActorPath]]
  private val listOfActiveDescriptors = List[FilterDescriptor]()

  override def preStart(): Unit = {
    mediator ! Subscribe("agents", self)
    mediator ! Subscribe("cluster", self)
    mediator ! Publish("cluster", ActorJoin("ClusterCapManager", self))
    log.info("ClusterCapabilitiesManager started.")
  }

  override def postStop(): Unit = {
    mediator ! Publish("cluster", ActorLeave("ClusterCapManager", self))
    mediator ! Unsubscribe("agents", self)
    mediator ! Unsubscribe("cluster", self)
    log.info("ClusterCapabilitiesManager stopped.")
  }

  override def receive: Receive = {
    case GetStandardDescriptors(selectedLanguage) =>
      sender ! StandardDescriptors(listOfFilterDescriptors.keys.toList.map(meta => meta.describe(selectedLanguage)))

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

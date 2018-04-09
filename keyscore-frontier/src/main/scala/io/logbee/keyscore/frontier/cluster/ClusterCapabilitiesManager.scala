package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorLogging, ActorPath, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.{AgentCapabilities, AgentLeaved}
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{ActiveDescriptors, GetActiveDescriptors, GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.sinks.{KafkaSink, StdOutSink}
import io.logbee.keyscore.frontier.sources.{HttpSource, KafkaSource}
import io.logbee.keyscore.model.filter.FilterDescriptor

import scala.collection.mutable


object ClusterCapabilitiesManager {
  def props(): Props = Props(new ClusterCapabilitiesManager())

  case object GetStandardDescriptors

  case class StandardDescriptors(listOfDescriptorsAndType: List[FilterDescriptor])

  case object GetActiveDescriptors

  case class ActiveDescriptors(listOfDescriptors: List[FilterDescriptor])

}

class ClusterCapabilitiesManager extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  private val listOfFilterDescriptors = mutable.Map.empty[FilterDescriptor, mutable.Set[ActorPath]]
  private val listOfActiveDescriptors = List[FilterDescriptor]()

  override def preStart(): Unit = {
    mediator ! Subscribe("agents", self)

    addDefaultDescriptors()

    log.info("StartUp complete")
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe("agents", self)
    log.info("Shutdown complete")
  }

  override def receive: Receive = {
    case GetStandardDescriptors =>
      sender ! StandardDescriptors(listOfFilterDescriptors.keySet.toList)

    case GetActiveDescriptors =>
      sender() ! ActiveDescriptors(listOfActiveDescriptors)

    case AgentCapabilities(filterDescriptors) =>
      filterDescriptors.foreach(descriptor => {
        listOfFilterDescriptors.getOrElseUpdate(descriptor, mutable.Set.empty) += sender.path
      })

    case AgentLeaved(ref) =>
      listOfFilterDescriptors.retain((_, paths) => {
        paths.retain(path => ref.path.address != path.address)
        paths.nonEmpty
      })

      addDefaultDescriptors()
  }

  private def addDefaultDescriptors(): Unit = {
    listOfFilterDescriptors ++= Map(
      AddFieldsFilter.descriptor -> mutable.Set.empty,
      GrokFilter.descriptor -> mutable.Set.empty,
      RemoveFieldsFilter.descriptor -> mutable.Set.empty,
      RetainFieldsFilter.descriptor -> mutable.Set.empty,
      KafkaSource.descriptor -> mutable.Set.empty,
      KafkaSink.descriptor -> mutable.Set.empty,
      HttpSource.descriptor -> mutable.Set.empty,
      StdOutSink.descriptor -> mutable.Set.empty
    )
  }
}

package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.AgentCapabilities
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

  private val listOfFilterDescriptors = mutable.Set.empty[FilterDescriptor]
  private val listOfActiveDescriptors = List[FilterDescriptor]()

  override def preStart(): Unit = {
    mediator ! Subscribe("agents", self)

    listOfFilterDescriptors ++= List(
        AddFieldsFilter.descriptor,
        GrokFilter.descriptor,
        RemoveFieldsFilter.descriptor,
        RetainFieldsFilter.descriptor,
        KafkaSource.descriptor,
        KafkaSink.descriptor,
        HttpSource.descriptor,
        StdOutSink.descriptor
    )

    log.info("StartUp complete")
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe("agents", self)
    log.info("Shutdown complete")
  }

  override def receive: Receive = {
    case GetStandardDescriptors=>
      sender ! StandardDescriptors(listOfFilterDescriptors.toList)

    case GetActiveDescriptors =>
      sender() ! ActiveDescriptors(listOfActiveDescriptors)

    case AgentCapabilities(filterDescriptors) =>
      listOfFilterDescriptors ++= filterDescriptors
  }
}

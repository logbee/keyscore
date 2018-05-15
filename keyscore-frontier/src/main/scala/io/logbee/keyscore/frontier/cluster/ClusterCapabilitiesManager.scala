package io.logbee.keyscore.frontier.cluster

import java.util.Locale

import akka.actor.{Actor, ActorLogging, ActorPath, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.{AgentCapabilities, AgentLeaved}
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{ActiveDescriptors, GetActiveDescriptors, GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.sinks.{KafkaSink, StdOutSink}
import io.logbee.keyscore.frontier.sources.{HttpSource, KafkaSource}
import io.logbee.keyscore.model.sink.FilterDescriptor

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object ClusterCapabilitiesManager {
  def props(): Props = Props(new ClusterCapabilitiesManager())

  case class GetStandardDescriptors(language: Locale)

  case class StandardDescriptors(listOfDescriptorsAndType: List[FilterDescriptor])

  case object GetActiveDescriptors

  case class ActiveDescriptors(listOfDescriptors: List[FilterDescriptor])

}

class ClusterCapabilitiesManager extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  private val listOfFilterDescriptors = mutable.Map.empty[mutable.Map[Locale, FilterDescriptor], mutable.Set[ActorPath]]
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
    case GetStandardDescriptors(selectedLanguage) =>
      sender ! StandardDescriptors(listOfFilterDescriptors.foldLeft(ListBuffer[FilterDescriptor]()) {
        (result, descriptorMap) =>
          result += descriptorMap._1(selectedLanguage)
      }.toList)

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

      addDefaultDescriptors()

  }

  private def addDefaultDescriptors(): Unit = {
    listOfFilterDescriptors ++= Map(
      KafkaSource.getDescriptors -> mutable.Set.empty,
      HttpSource.getDescriptors -> mutable.Set.empty,
      RemoveFieldsFilter.getDescriptors -> mutable.Set.empty,
      RetainFieldsFilter.getDescriptors -> mutable.Set.empty,
      KafkaSink.getDescriptors -> mutable.Set.empty,
      StdOutSink.getDescriptors -> mutable.Set.empty,
      AddFieldsFilter.getDescriptors -> mutable.Set.empty,
      GrokFilter.getDescriptors -> mutable.Set.empty,
    )

  }
}

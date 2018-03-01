package io.logbee.keyscore.frontier.stream

import akka.actor.{Actor, ActorLogging, Props}
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.{HttpSource, KafkaSource}
import io.logbee.keyscore.frontier.stream.FilterDescriptorManager.{ActiveDescriptors, GetActiveDescriptors, GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.model.filter.FilterDescriptor


object FilterDescriptorManager {
  def props(): Props = Props(new FilterDescriptorManager())

  case object GetStandardDescriptors

  case class StandardDescriptors(listOfDescriptorsAndType: List[FilterDescriptor])

  case object GetActiveDescriptors

  case class ActiveDescriptors(listOfDescriptors: List[FilterDescriptor])

}


class FilterDescriptorManager extends Actor with ActorLogging {

  val listOfFilterDescriptors = List[FilterDescriptor](
    AddFieldsFilter.descriptor, GrokFilter.descriptor, RemoveFieldsFilter.descriptor, RetainFieldsFilter.descriptor,KafkaSource.descriptor,KafkaSink.descriptor,HttpSource.descriptor
  )
  val listOfActiveDescriptors = List[FilterDescriptor]()

  override def preStart(): Unit = {
    log.info("FilterDescriptorManager started")
  }

  override def postStop(): Unit = {
    log.info("FilterDescriptorManager stopped")
  }

  override def receive: Receive = {
    case GetStandardDescriptors=>
      sender ! StandardDescriptors(listOfFilterDescriptors)


    case GetActiveDescriptors =>
      sender() ! ActiveDescriptors(listOfActiveDescriptors)
  }

}

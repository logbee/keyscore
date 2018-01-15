package io.logbee.keyscore.frontier.stream

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.stream.FilterDescriptorManager.{ActiveDescriptors, GetActiveDescriptors, GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.model.filter.FilterDescriptor
import io.logbee.keyscore.model.filter.FilterDescriptor.FilterDescriptor


object FilterDescriptorManager {
  def props(): Props = Props(new FilterDescriptorManager())

  case class GetStandardDescriptors()
  case class StandardDescriptors(listOfDescriptors: List[FilterDescriptor])

  case class GetActiveDescriptors()
  case class ActiveDescriptors(listOfDescriptors: List[FilterDescriptor])
}


class FilterDescriptorManager extends Actor with ActorLogging{

  val listOfStandardDescriptors = List[FilterDescriptor] (
    AddFieldsFilter.descriptor, GrokFilter.descriptor, RemoveFieldsFilter.descriptor, RetainFieldsFilter.descriptor
  )
  val listOfActiveDescriptors = List[FilterDescriptor] ()

  override def preStart(): Unit = {
    log.info("FilterDescriptorManager started")
  }

  override def postStop(): Unit = {
    log.info("FilterDescriptorManager stopped")
  }

  override def receive: Receive = {
    case GetStandardDescriptors =>
      sender() ! StandardDescriptors(listOfStandardDescriptors)
    case GetActiveDescriptors =>
      sender() ! ActiveDescriptors(listOfActiveDescriptors)
  }

}

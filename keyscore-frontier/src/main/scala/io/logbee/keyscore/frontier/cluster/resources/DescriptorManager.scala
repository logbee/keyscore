package io.logbee.keyscore.frontier.cluster.resources

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages._
import io.logbee.keyscore.commons.{DescriptorService, HereIam, WhoIs}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

/**
  * The DescriptorManager holds a map for all Descriptors and <br>
  * resolves a DescriptorRef to the specific Descriptor.
  */
object DescriptorManager {

  def apply(): Props = Props(new DescriptorManager())

}

class DescriptorManager extends Actor with ActorLogging {

  private val descriptors = scala.collection.mutable.Map.empty[DescriptorRef, Descriptor]

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
    log.debug(s" started.")
  }

  override def postStop(): Unit = {
    log.debug(s" stopped.")
  }

  override def receive: Receive = {
    case StoreDescriptorRequest(descriptor) =>
      log.debug(s"Received StoreDescriptorRequest for $descriptor")
      descriptors.put(descriptor.ref, descriptor)
      sender ! StoreDescriptorResponse

    case GetAllDescriptorsRequest =>
      log.debug("Received GetAllDescriptorsRequest")
      sender ! GetAllDescriptorsResponse(descriptors.toMap)

    case DeleteDescriptorRequest(ref) =>
      log.debug(s"Received DeleteDescriptorRequest for $ref")
      descriptors.remove(ref)
      sender ! DeleteDescriptorResponse

    case DeleteAllDescriptorsRequest =>
      log.debug("Received DeleteAllDescriptorsRequest")
      descriptors.clear()
      sender ! DeleteAllDescriptorsResponse

    case GetDescriptorRequest(ref) =>
      log.debug(s"Received GetDescriptorRequest for $ref")
      sender ! GetDescriptorResponse(descriptors.get(ref))

    case UpdateDescriptorRequest(descriptor) =>
      log.debug(s"Received UpdateDescriptorRequest for $descriptor")
      if (descriptors.contains(descriptor.ref)) {
        log.debug(s"Updated Descriptor for $descriptor")
        descriptors.put(descriptor.ref, descriptor)
        sender ! UpdateDescriptorSuccessResponse
      } else {
        log.debug(s"Couldn't update Descriptor for $descriptor")
        sender ! UpdateDescriptorFailureResponse
      }

    case WhoIs(DescriptorService) =>
      log.debug("Received WhoIs(DescriptorService)")
      sender ! HereIam(DescriptorService, self)
  }
}

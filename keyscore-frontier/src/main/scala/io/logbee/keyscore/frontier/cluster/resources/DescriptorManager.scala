package io.logbee.keyscore.frontier.cluster.resources

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages._
import io.logbee.keyscore.commons.{DescriptorService, HereIam, WhoIs}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

object DescriptorManager {

  def apply(): Props = Props(new DescriptorManager())

}

class DescriptorManager extends Actor with ActorLogging {

  private val descriptors = scala.collection.mutable.Map.empty[DescriptorRef, Descriptor]

  private val mediator = DistributedPubSub(context.system).mediator


  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
  }

  override def postStop(): Unit = super.postStop()

  override def receive: Receive = {
    case StoreDescriptorRequest(descriptor) =>
      descriptors.put(descriptor.ref, descriptor)

    case DeleteDescriptorRequest(ref) =>
      descriptors.remove(ref)

    case GetDescriptorRequest(ref) =>
      sender ! GetDescriptorResponse(descriptors.get(ref))

    case WhoIs(DescriptorService) =>
      sender ! HereIam(DescriptorService, self)
  }
}

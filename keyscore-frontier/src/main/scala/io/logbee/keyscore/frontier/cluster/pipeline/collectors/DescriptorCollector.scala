package io.logbee.keyscore.frontier.cluster.pipeline.collectorsCreatePipeline

import akka.actor.{Actor, ActorRef, Props}
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages.{GetDescriptorRequest, GetDescriptorResponse}
import io.logbee.keyscore.model.blueprint.SealedBlueprint
import io.logbee.keyscore.model.blueprint.ToBase.sealedToDescriptor
import io.logbee.keyscore.model.descriptor.Descriptor

import scala.concurrent.duration._


/**
  * Returns a List of Descriptors from a list of DescriptorRefs after collecting them from the DescriptorManager.
  */
object DescriptorCollector {
  def apply(receiver: ActorRef, sealedBlueprints: List[SealedBlueprint], descriptorManager: ActorRef) = Props(new DescriptorCollector(receiver, sealedBlueprints, descriptorManager))

}
class DescriptorCollector(receiver: ActorRef, sealedBlueprints: List[SealedBlueprint], descriptorManager: ActorRef) extends Actor {
  import context.{dispatcher, system}

  private var descriptors = scala.collection.mutable.ListBuffer.empty[Descriptor]

  override def preStart(): Unit = {
    sealedBlueprints.foreach( current => {
      descriptorManager ! GetDescriptorRequest(current.descriptorRef)
    })
    system.scheduler.scheduleOnce(5 seconds) {
      receiver !
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case  GetDescriptorResponse(current) => current match {
      case Some(descriptor) => descriptors += descriptor
      case _ =>
    }
  }
}

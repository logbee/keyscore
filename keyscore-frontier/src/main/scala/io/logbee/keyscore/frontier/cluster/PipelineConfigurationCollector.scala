package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorRef, Props}
import io.logbee.keyscore.commons.pipeline.PipelineConfigurationResponse
import io.logbee.keyscore.model.PipelineConfiguration

import scala.concurrent.duration._
import scala.collection.mutable

object PipelineConfigurationCollector {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new PipelineConfigurationCollector(receiver, children))
}

class PipelineConfigurationCollector(receiver: ActorRef, children: Iterable[ActorRef]) extends Actor {
  import context.{dispatcher, system}

  private var configs = mutable.ListBuffer.empty[PipelineConfiguration]

  override def preStart(): Unit = {
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! PipelineConfigurationResponse(configs.toList)
      context.stop(self)
    }
  }


  override def receive: Receive = {
    case config: PipelineConfiguration =>
      configs += config
  }

}
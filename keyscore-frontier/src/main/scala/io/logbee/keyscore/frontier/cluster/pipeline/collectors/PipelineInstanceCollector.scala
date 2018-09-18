package io.logbee.keyscore.frontier.cluster.pipeline.collectors

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import io.logbee.keyscore.commons.pipeline.{PipelineInstanceResponse, RequestPipelineInstance}
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.PipelineInstanceCollector.Finish
import io.logbee.keyscore.model.PipelineInstance

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Returns a list of PipelineInstances.
  */
object PipelineInstanceCollector {
  def apply(receiver: ActorRef, agents: Seq[ActorRef], localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection)(implicit timeout: FiniteDuration) = Props(new PipelineInstanceCollector(receiver, agents, localPipelineManagerResolution, timeout))

  private case object Finish
}

class PipelineInstanceCollector(receiver: ActorRef, agents: Seq[ActorRef], localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection, timeout: FiniteDuration) extends Actor with ActorLogging {

  import context.{dispatcher, system}

  private val states = mutable.ListBuffer.empty[PipelineInstance]

  override def preStart(): Unit = {
    agents.foreach(agent => {
      localPipelineManagerResolution(agent, context) ! RequestPipelineInstance
    })
    system.scheduler.scheduleOnce(timeout, self, Finish)
  }

  override def receive: Receive = {
    case state: PipelineInstance =>
      states += state

    case Finish =>
      receiver ! PipelineInstanceResponse(states.toList)
      context.stop(self)
  }
}

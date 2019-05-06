package io.logbee.keyscore.frontier.cluster.pipeline.collectors

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import io.logbee.keyscore.commons.pipeline.{PipelineInstanceResponse, RequestPipelineInstance}
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.PipelineInstanceCollector.CheckStates
import io.logbee.keyscore.model.PipelineInstance

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Returns a list of all received PipelineInstances for each required agent.
  *
  */
object PipelineInstanceCollector {
  def apply(receiver: ActorRef, agents: Seq[ActorRef], localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection)(implicit timeout: FiniteDuration) = Props(new PipelineInstanceCollector(receiver, agents, localPipelineManagerResolution, timeout))

  private case object CheckStates

  case object PipelineInstanceResponseFailure
}

class PipelineInstanceCollector(receiver: ActorRef, agents: Seq[ActorRef], localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection, timeout: FiniteDuration) extends Actor with ActorLogging {

  import context.{dispatcher, system}

  private val states = mutable.ListBuffer.empty[PipelineInstance]

  override def preStart(): Unit = {
    log.debug(" started.")
    agents.foreach(agent => {
      localPipelineManagerResolution(agent, context) ! RequestPipelineInstance
    })
    system.scheduler.scheduleOnce(timeout, self, CheckStates)
  }

  override def receive: Receive = {
    case state: PipelineInstance =>
      states += state

    case CheckStates =>
      receiver ! PipelineInstanceResponse(states.toList)
      context.stop(self)
  }
}

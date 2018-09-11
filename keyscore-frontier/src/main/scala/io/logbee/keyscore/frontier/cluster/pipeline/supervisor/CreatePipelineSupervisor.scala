package io.logbee.keyscore.frontier.cluster.pipeline.supervisor

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetPipelineBlueprintRequest, GetPipelineBlueprintResponse}
import io.logbee.keyscore.commons.{BlueprintService, HereIam, WhoIs}
import io.logbee.keyscore.frontier.cluster.pipeline.supervisor.CreatePipelineSupervisor.CreatePipelineRequest
import io.logbee.keyscore.model.blueprint.BlueprintRef

object CreatePipelineSupervisor {

  case class CreatePipelineRequest(blueprintRef: BlueprintRef, sender: ActorRef)

  case object CreatePipelineResponse

}

class CreatePipelineSupervisor extends Actor with  ActorLogging {

  private val mediator: ActorRef = DistributedPubSub(context.system).mediator
  private var blueprintManager: ActorRef = _
  override def preStart(): Unit = {
    mediator ! WhoIs(BlueprintService)
  }

  override def receive: Receive = {
    case HereIam(BlueprintService, ref) => {
      blueprintManager = ref
      context.become(running)
    }
  }

  private def running: Receive = {
    case CreatePipelineRequest(blueprintRef, sender) =>
      blueprintManager ! GetPipelineBlueprintRequest(blueprintRef)

    case GetPipelineBlueprintResponse(blueprint) =>
//      placeholder ! GetAgentForPipeline(blueprint)

//    case GetAgentForPiplineResonse(agentref) =>
//      pipelineSchedulerSelector(agent, context) ! CreatePipelineOrder(pipelineBlueprint)

  }
}

package io.logbee.keyscore.frontier.cluster.pipeline.subordinates

import java.util.UUID

import akka.actor
import akka.actor.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetPipelineBlueprintRequest, GetPipelineBlueprintResponse, StoreBlueprintRequest, StorePipelineBlueprintRequest}
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages.StoreConfigurationRequest
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}
import io.logbee.keyscore.model.configuration.Configuration

object PipelineImporter {

  case class ImportPipelineRequest(pipeline: PipelineBlueprint, blueprints: List[SealedBlueprint], configurations: List[Configuration])

  case class ImportPipelineSuccessResponse(id: UUID)

  case class ImportPipelineConflictResponse(id: UUID)

  case class ImportPipelineFailureResponse(id: UUID)

  def importPipeline(pipeline: PipelineBlueprint, blueprints: List[SealedBlueprint], configurations: List[Configuration], blueprintManager: actor.ActorRef, configurationManager: actor.ActorRef, replayTo: actor.ActorRef)(implicit parent: ActorContext): Unit = {

    import akka.actor.typed.scaladsl.adapter._

    parent.spawnAnonymous(Behaviors.setup[AnyRef]( context => {

      val self = context.self.toUntyped

      blueprintManager tell (GetPipelineBlueprintRequest(BlueprintRef(pipeline.ref.uuid.toString)), self)

      Behaviors.receiveMessage {

        case GetPipelineBlueprintResponse(None) =>
          blueprintManager tell (StorePipelineBlueprintRequest(pipeline), self)
          blueprints.foreach(blueprint => blueprintManager tell (StoreBlueprintRequest(blueprint), self))
          configurations.foreach(configuration => configurationManager tell (StoreConfigurationRequest(configuration), self))
          replayTo tell (ImportPipelineSuccessResponse(UUID.fromString(pipeline.ref.uuid)), parent.self)
          Behaviors.stopped

        case GetPipelineBlueprintResponse(Some(_)) =>
          replayTo tell (ImportPipelineConflictResponse(UUID.fromString(pipeline.ref.uuid)), parent.self)
          Behaviors.stopped
      }
    }))
  }
}

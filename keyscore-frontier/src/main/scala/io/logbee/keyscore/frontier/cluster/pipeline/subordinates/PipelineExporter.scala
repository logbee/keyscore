package io.logbee.keyscore.frontier.cluster.pipeline.subordinates

import java.util.UUID

import akka.actor
import akka.actor.ActorContext
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetBlueprintRequest, GetBlueprintResponse, GetPipelineBlueprintRequest, GetPipelineBlueprintResponse}
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages.{GetConfigurationRequest, GetConfigurationSuccess}
import io.logbee.keyscore.model.blueprint.{BlueprintRef, BranchBlueprint, FilterBlueprint, MergeBlueprint, PipelineBlueprint, SealedBlueprint, SinkBlueprint, SourceBlueprint}
import io.logbee.keyscore.model.configuration.Configuration

object PipelineExporter {

  case class ExportPipelineRequest(id: UUID)

  case class ExportPipelineResponse(pipeline: PipelineBlueprint, blueprints: List[SealedBlueprint], configurations: List[Configuration])

  case class ExportPipelineNotFoundResponse(id: UUID)

  case class ExportPipelineFailureResponse()

  def exportPipeline(id: UUID, blueprintManager: actor.ActorRef, configurationManager: actor.ActorRef, replayTo: actor.ActorRef)(implicit parent: ActorContext): Unit = {

    import akka.actor.typed.scaladsl.adapter._

    parent.spawnAnonymous(Behaviors.setup[AnyRef]( context => {

      val self = context.self.toUntyped

      blueprintManager tell (GetPipelineBlueprintRequest(BlueprintRef(id.toString)), self)

      Behaviors.receiveMessage {

        case GetPipelineBlueprintResponse(Some(pipelineBlueprint)) =>
          pipelineBlueprint.blueprints.foreach(ref => blueprintManager tell (GetBlueprintRequest(BlueprintRef(ref.uuid)), self))

          def export(pipeline: PipelineBlueprint, blueprints: List[SealedBlueprint], configurations: List[Configuration]): Behavior[AnyRef] = {

            if (blueprints.size < pipeline.blueprints.size || configurations.size < pipeline.blueprints.size) {

              Behaviors.receiveMessage {

                case GetBlueprintResponse(Some(blueprint: SourceBlueprint)) =>
                  configurationManager tell (GetConfigurationRequest(blueprint.configuration), self)
                  export(pipeline, blueprints :+ blueprint, configurations)

                case GetBlueprintResponse(Some(blueprint: FilterBlueprint)) =>
                  configurationManager tell (GetConfigurationRequest(blueprint.configuration), self)
                  export(pipeline, blueprints :+ blueprint, configurations)

                case GetBlueprintResponse(Some(blueprint: SinkBlueprint)) =>
                  configurationManager tell (GetConfigurationRequest(blueprint.configuration), self)
                  export(pipeline, blueprints :+ blueprint, configurations)

                case GetBlueprintResponse(Some(blueprint: BranchBlueprint)) =>
                  configurationManager tell (GetConfigurationRequest(blueprint.configuration), self)
                  export(pipeline, blueprints :+ blueprint, configurations)

                case GetBlueprintResponse(Some(blueprint: MergeBlueprint)) =>
                  configurationManager tell (GetConfigurationRequest(blueprint.configuration), self)
                  export(pipeline, blueprints :+ blueprint, configurations)

                case GetConfigurationSuccess(configuration: Configuration) =>
                  export(pipeline, blueprints, configurations :+ configuration)

                case _ =>
                  replayTo tell (ExportPipelineFailureResponse(), parent.self)
                  Behaviors.stopped
              }
            }
            else {
              replayTo tell (ExportPipelineResponse(pipeline, blueprints, configurations), parent.self)
              Behaviors.stopped
            }
          }

          export(pipelineBlueprint, List.empty, List.empty)

        case GetPipelineBlueprintResponse(None) =>
          replayTo tell (ExportPipelineNotFoundResponse(id), parent.self)
          Behaviors.stopped

        case _ =>
          replayTo tell (ExportPipelineFailureResponse(), parent.self)
          Behaviors.stopped
      }
    }))
  }
}

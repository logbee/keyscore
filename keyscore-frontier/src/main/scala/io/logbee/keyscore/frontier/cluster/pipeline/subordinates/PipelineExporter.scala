package io.logbee.keyscore.frontier.cluster.pipeline.subordinates

import java.util.UUID

import akka.actor
import akka.actor.ActorContext
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetBlueprintRequest, GetBlueprintResponse, GetPipelineBlueprintRequest, GetPipelineBlueprintResponse}
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages.{GetConfigurationRequest, GetConfigurationSuccess}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager.{ExportPipelineFailureResponse, ExportPipelineNotFoundResponse, ExportPipelineResponse}
import io.logbee.keyscore.model.blueprint.{BlueprintRef, BranchBlueprint, FilterBlueprint, MergeBlueprint, PipelineBlueprint, SealedBlueprint, SinkBlueprint, SourceBlueprint}
import io.logbee.keyscore.model.configuration.Configuration

object PipelineExporter {

  def export(id: UUID, blueprintManager: actor.ActorRef, configurationManager: actor.ActorRef, replayTo: actor.ActorRef)(implicit context: ActorContext): Unit = {
    import akka.actor.typed.scaladsl.adapter._

    context.spawnAnonymous(Behaviors.setup[AnyRef]( context => {

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
                  replayTo tell (ExportPipelineFailureResponse(), context.self.toUntyped)
                  Behaviors.stopped
              }
            }
            else {
              replayTo tell (ExportPipelineResponse(pipeline, blueprints, configurations), context.self.toUntyped)
              Behaviors.stopped
            }
          }

          export(pipelineBlueprint, List.empty, List.empty)

        case GetPipelineBlueprintResponse(None) =>
          replayTo tell (ExportPipelineNotFoundResponse(id), context.self.toUntyped)
          Behaviors.stopped

        case _ =>
          replayTo tell (ExportPipelineFailureResponse(), context.self.toUntyped)
          Behaviors.stopped
      }
    }))
  }
}

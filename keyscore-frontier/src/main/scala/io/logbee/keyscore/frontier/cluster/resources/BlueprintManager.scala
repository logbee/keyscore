package io.logbee.keyscore.frontier.cluster.resources

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages._
import io.logbee.keyscore.commons.{DescriptorService, HereIam, WhoIs}
import io.logbee.keyscore.model.blueprint.ToBaseBlueprint.sealedToBase
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}


class BlueprintManager extends Actor with ActorLogging {

  private val pipelineBlueprints = scala.collection.mutable.Map.empty[BlueprintRef, PipelineBlueprint]
  private val blueprints = scala.collection.mutable.Map.empty[BlueprintRef, SealedBlueprint]

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
  }

  override def postStop(): Unit = super.postStop()

  override def receive: Receive = {

    //Single Request
    case StorePipelineBlueprintRequest(pipelineBlueprint) =>
      pipelineBlueprints.put(pipelineBlueprint.ref, pipelineBlueprint)

    case StoreBlueprintRequest(blueprint) =>
      blueprints.put(blueprint.ref, blueprint)

    case GetPipelineBlueprintRequest(ref) =>
      sender ! GetPipelineBlueprintResponse(pipelineBlueprints.get(ref))

    case GetBlueprintRequest(ref) =>
      sender ! GetBlueprintResponse(blueprints.get(ref))

    case DeletePipelineBlueprintsRequest(ref) =>
      pipelineBlueprints.remove(ref)

    case DeleteBlueprintRequest(ref) =>
      blueprints.remove(ref)

    //Multiple Request

    case GetAllPipelineBlueprintsRequest =>
      sender ! GetAllPipelineBlueprintsResponse(pipelineBlueprints.values.toList)

    case GetAllBlueprintsRequest =>
      sender ! GetAllBlueprintsResponse(blueprints.values.toList)

    case WhoIs(DescriptorService) =>
      sender ! HereIam(DescriptorService, self)
  }
}

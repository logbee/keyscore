package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import io.logbee.keyscore.agent.pipeline.BlueprintMaterializer.{Initialize, InstantiateStage, ResolveBlueprint}
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetBlueprintRequest, GetBlueprintResponse}
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages.{GetConfigurationFailure, GetConfigurationRequest, GetConfigurationSuccess}
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages.{GetDescriptorRequest, GetDescriptorResponse}
import io.logbee.keyscore.commons.util.ServiceDiscovery.discover
import io.logbee.keyscore.model.blueprint._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.descriptor.Descriptor
import io.logbee.keyscore.pipeline.api.stage.StageContext

import scala.util.{Failure, Success}

object BlueprintMaterializer {

  def apply(stagecontext: StageContext, blueprintRef: BlueprintRef, filterManager: ActorRef): Props = Props(new BlueprintMaterializer(stagecontext, blueprintRef, filterManager))

  private case class Initialize(blueprintManager: ActorRef, descriptorManager: ActorRef, configurationManager: ActorRef)

  private case object StartMaterializing

  private case object InstantiateStage

  private case object ResolveBlueprint
}

class BlueprintMaterializer(stageContext: StageContext, blueprintRef: BlueprintRef, filterManager: ActorRef, initialBlueprintManager: Option[ActorRef] = None, initialDescriptorManager: Option[ActorRef] = None, initialConfigurationManager: Option[ActorRef] = None) extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  private implicit val ec = context.dispatcher

  import context.{become, parent}

  override def preStart(): Unit = {

    log.info(s"Started for blueprint <${blueprintRef.uuid}>.")

    if (initialBlueprintManager.isEmpty || initialDescriptorManager.isEmpty || initialConfigurationManager.isEmpty) {
      discover(Seq(BlueprintService, DescriptorService, ConfigurationService)).onComplete {
        case Success(services) =>
          self ! Initialize(services(BlueprintService), services(DescriptorService), services(ConfigurationService))
        case Failure(exception) =>
        // TODO: Handle this case!
      }
    }
    else {
      self ! Initialize(initialBlueprintManager.get, initialDescriptorManager.get, initialConfigurationManager.get)
    }
  }

  override def postStop(): Unit = {
    log.info(s"Stopped for blueprint <${blueprintRef.uuid}>.")
  }

  override def receive: Receive = {

    case Initialize(blueprintManager, descriptorManager, configurationManager) =>
      log.debug("Initializing.")
      become(initializing(blueprintManager, descriptorManager, configurationManager), discardOld = true)
      self ! ResolveBlueprint
  }

  private def initializing(blueprintManager: ActorRef, descriptorManager: ActorRef, configurationManager: ActorRef): Receive = {

    case ResolveBlueprint =>
      log.debug(s"Resolving blueprint <${blueprintRef.uuid}>")
      blueprintManager ! GetBlueprintRequest(blueprintRef)

    case GetBlueprintResponse(Some(blueprint)) =>
      blueprint match {
        case filterBlueprint: FilterBlueprint =>
          descriptorManager ! GetDescriptorRequest(filterBlueprint.descriptor)
          configurationManager ! GetConfigurationRequest(filterBlueprint.configuration)
          become(preparing(filterBlueprint, Preparation()))
        case sourceBlueprint: SourceBlueprint =>
          descriptorManager ! GetDescriptorRequest(sourceBlueprint.descriptor)
          configurationManager ! GetConfigurationRequest(sourceBlueprint.configuration)
          become(preparing(sourceBlueprint, Preparation()))
        case sinkBlueprint: SinkBlueprint =>
          descriptorManager ! GetDescriptorRequest(sinkBlueprint.descriptor)
          configurationManager ! GetConfigurationRequest(sinkBlueprint.configuration)
          become(preparing(sinkBlueprint, Preparation()))
      }
  }

  private def preparing(blueprint: FilterBlueprint, preparation: Preparation): Receive = {
    case GetConfigurationSuccess(configuration) =>
      log.debug(s"Got configuration of blueprint <${blueprint.ref}>: $configuration")
      maybeBecomeMaterializing(blueprint, preparation.copy(configuration = Option(configuration)))

    case GetConfigurationFailure(_) =>
      log.error(s"Did not get configuration <${blueprint.configuration.uuid}> of blueprint <${blueprint.ref.uuid}>")
      context.stop(self)

    case GetDescriptorResponse(descriptor) =>
      if (descriptor.isDefined) {
        log.debug(s"Got descriptor of blueprint <${blueprint.ref}>: ${descriptor.get}")
        maybeBecomeMaterializing(blueprint, preparation.copy(descriptor = descriptor))
      } else {
        log.error(s"Did not get descriptor <${blueprint.descriptor.uuid}> of blueprint <${blueprint.ref.uuid}>")
        context.stop(self)
      }
  }

  private def preparing(blueprint: SinkBlueprint, preparation: Preparation): Receive = {
    case GetConfigurationSuccess(configuration) =>
      log.debug(s"Got configuration of blueprint <${blueprint.ref}>: $configuration")
      maybeBecomeMaterializing(blueprint, preparation.copy(configuration = Option(configuration)))

    case GetConfigurationFailure(_) =>
      log.error(s"Did not get configuration <${blueprint.configuration.uuid}> of blueprint <${blueprint.ref.uuid}>")
      context.stop(self)

    case GetDescriptorResponse(descriptor) =>
      if (descriptor.isDefined) {
        log.debug(s"Got descriptor of blueprint <${blueprint.ref}>: ${descriptor.get}")
        maybeBecomeMaterializing(blueprint, preparation.copy(descriptor = descriptor))
      } else {
        log.error(s"Did not get descriptor <${blueprint.descriptor.uuid}> of blueprint <${blueprint.ref.uuid}>")
        context.stop(self)
      }
  }

  private def preparing(blueprint: SourceBlueprint, preparation: Preparation): Receive = {
    case GetConfigurationSuccess(configuration) =>
      log.debug(s"Got configuration of blueprint <${blueprint.ref}>: $configuration")
      maybeBecomeMaterializing(blueprint, preparation.copy(configuration = Option(configuration)))

    case GetConfigurationFailure(_) =>
      log.error(s"Did not get configuration <${blueprint.configuration.uuid}> of blueprint <${blueprint.ref.uuid}>")
      context.stop(self)

    case GetDescriptorResponse(descriptor) =>
      if (descriptor.isDefined) {
        log.debug(s"Got descriptor of blueprint <${blueprint.ref}>: ${descriptor.get}")
        maybeBecomeMaterializing(blueprint, preparation.copy(descriptor = descriptor))
      } else {
        log.error(s"Did not get descriptor <${blueprint.descriptor.uuid}> of blueprint <${blueprint.ref.uuid}>")
        context.stop(self)
      }
  }

  private def materializing(blueprint: FilterBlueprint, materialization: Materialization): Receive = {
    case InstantiateStage =>
        log.info(s"Creating FilterStage: ${blueprint.ref.uuid}")
        filterManager ! CreateFilterStage(blueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)

    case filterStageMessage: FilterStageCreated =>
      parent ! filterStageMessage
  }

  private def materializing(blueprint: SinkBlueprint, materialization: Materialization): Receive = {
    case InstantiateStage =>
        log.info(s"Creating SinkStage: ${blueprint.ref.uuid}")
        filterManager ! CreateSinkStage(blueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)

    case sinkStageMessage: SinkStageCreated =>
      parent ! sinkStageMessage
  }

  private def materializing(blueprint: SourceBlueprint, materialization: Materialization): Receive = {
    case InstantiateStage =>
        log.info(s"Creating SourceStage: ${blueprint.ref.uuid}")
        filterManager ! CreateSourceStage(blueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)

    case sourceStageMessage: SourceStageCreated =>
      parent ! sourceStageMessage
  }

  private def maybeBecomeMaterializing(blueprint: FilterBlueprint, preparation: Preparation): Unit = {
    if (preparation.isComplete) {
      become(materializing(blueprint, Materialization(preparation.descriptor.get, preparation.configuration.get)), discardOld = true)
      self ! InstantiateStage
    }
    else {
      become(preparing(blueprint, preparation), discardOld = true)
    }
  }

  private def maybeBecomeMaterializing(blueprint: SinkBlueprint, preparation: Preparation): Unit = {
    if (preparation.isComplete) {
      become(materializing(blueprint, Materialization(preparation.descriptor.get, preparation.configuration.get)), discardOld = true)
      self ! InstantiateStage
    }
    else {
      become(preparing(blueprint, preparation), discardOld = true)
    }
  }

  private def maybeBecomeMaterializing(blueprint: SourceBlueprint, preparation: Preparation): Unit = {
    if (preparation.isComplete) {
      become(materializing(blueprint, Materialization(preparation.descriptor.get, preparation.configuration.get)), discardOld = true)
      self ! InstantiateStage
    }
    else {
      become(preparing(blueprint, preparation), discardOld = true)
    }
  }

  case class Preparation(descriptor: Option[Descriptor] = None, configuration: Option[Configuration] = None) {
    def isComplete: Boolean = descriptor.isDefined && configuration.isDefined
  }

  case class Materialization(descriptor: Descriptor, configuration: Configuration)
}

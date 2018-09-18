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
import io.logbee.keyscore.model.blueprint.BlueprintWrapper.wrap
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
    log.info(s"Stopped <${blueprintRef.uuid}>")
  }

  override def receive: Receive = {

    case Initialize(blueprintManager, descriptorManager, configurationManager) =>
      log.debug(s"Initializing <${blueprintRef.uuid}>")
      become(initializing(blueprintManager, descriptorManager, configurationManager), discardOld = true)
      self ! ResolveBlueprint
  }

  private def initializing(blueprintManager: ActorRef, descriptorManager: ActorRef, configurationManager: ActorRef): Receive = {

    case ResolveBlueprint =>
      log.debug(s"Resolving blueprint <${blueprintRef.uuid}>")
      blueprintManager ! GetBlueprintRequest(blueprintRef)

    case GetBlueprintResponse(Some(blueprint)) =>
      log.info(s"Resolved blueprint: $blueprint")
      val wrapper = wrap(blueprint)
      descriptorManager ! GetDescriptorRequest(wrapper.descriptorRef)
      configurationManager ! GetConfigurationRequest(wrapper.configurationRef)
      become(preparing(wrapper, Preparation()))
  }

  private def preparing(wrapper: BlueprintWrapper[_], preparation: Preparation): Receive = {
    case GetConfigurationSuccess(configuration) =>
      log.debug(s"Resolved configuration of blueprint <${wrapper.blueprintRef.uuid}>: $configuration")
      maybeBecomeMaterializing(wrapper, preparation.copy(configuration = Option(configuration)))

    case GetConfigurationFailure(_) =>
      log.error(s"Could not resolve configuration <${wrapper.configurationRef.uuid}> of blueprint <${wrapper.blueprintRef.uuid}>")
      context.stop(self)

    case GetDescriptorResponse(descriptor) =>
      if (descriptor.isDefined) {
        log.debug(s"Resolved descriptor of blueprint <${wrapper.blueprintRef.uuid}>: ${descriptor.get}")
        maybeBecomeMaterializing(wrapper, preparation.copy(descriptor = descriptor))
      } else {
        log.error(s"Could not resolve descriptor <${wrapper.descriptorRef.uuid}> of blueprint <${wrapper.blueprintRef.uuid}>")
        context.stop(self)
      }
  }

  private def materializing(wrapper: BlueprintWrapper[_], materialization: Materialization): Receive = {
    case InstantiateStage =>
      wrapper.blueprint match {
        case blueprint: FilterBlueprint =>
          log.debug(s"Initiated instantiation of FilterStage from blueprint <${wrapper.blueprintRef.uuid}>")
          filterManager ! CreateFilterStage(blueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)
        case blueprint: SourceBlueprint =>
          log.debug(s"Initiated instantiation of SourceStage from blueprint <${wrapper.blueprintRef.uuid}>")
          filterManager ! CreateSourceStage(blueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)
        case blueprint: SinkBlueprint =>
          log.debug(s"Initiated instantiation of SinkStage from blueprint <${wrapper.blueprintRef.uuid}>")
          filterManager ! CreateSinkStage(blueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)
        case blueprint: BranchBlueprint =>
          log.debug(s"Initiated instantiation of BranchStage from blueprint <${wrapper.blueprintRef.uuid}>")
          filterManager ! CreateBranchStage(blueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)
        case blueprint: MergeBlueprint =>
          log.debug(s"Initiated instantiation of MergeStage from blueprint <${wrapper.blueprintRef.uuid}>")
          filterManager ! CreateMergeStage(blueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)
      }

    case message: StageCreated =>
      log.debug(s"Finishing materialization of blueprint <${blueprintRef.uuid}>")
      parent ! message
      context.stop(self)
  }

  private def maybeBecomeMaterializing(wrapper: BlueprintWrapper[_], preparation: Preparation): Unit = {
    if (preparation.isComplete) {
      become(materializing(wrapper, Materialization(preparation.descriptor.get, preparation.configuration.get)), discardOld = true)
      self ! InstantiateStage
    }
    else {
      become(preparing(wrapper, preparation), discardOld = true)
    }
  }

  case class Preparation(descriptor: Option[Descriptor] = None, configuration: Option[Configuration] = None) {
    def isComplete: Boolean = descriptor.isDefined && configuration.isDefined
  }

  case class Materialization(descriptor: Descriptor, configuration: Configuration)
}

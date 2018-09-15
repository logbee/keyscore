package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import io.logbee.keyscore.agent.pipeline.BlueprintMaterializer.{InstantiateStage, StartMaterializing}
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.cluster.Topics.WhoIsTopic
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages.{GetConfigurationRequest, GetConfigurationResponse}
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages.{GetDescriptorRequest, GetDescriptorResponse}
import io.logbee.keyscore.model.blueprint.{FilterBlueprint, SealedBlueprint, SinkBlueprint, SourceBlueprint}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.descriptor.Descriptor
import io.logbee.keyscore.pipeline.api.stage.StageContext

object BlueprintMaterializer {

  def apply(stagecontext: StageContext, blueprint: SealedBlueprint, filterManager: ActorRef): Props = Props(new BlueprintMaterializer(stagecontext, blueprint, filterManager))

  private case object StartMaterializing

  private case object InstantiateStage
}

class BlueprintMaterializer(stageContext: StageContext, blueprint: SealedBlueprint, filterManager: ActorRef, initialDescriptorManager: Option[ActorRef] = None, initialConfigurationManager: Option[ActorRef] = None) extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  private var configurationMananger: ActorRef = null
  private var descriptorMananger: ActorRef = null

  import context.{become, parent}

  override def preStart(): Unit = {

    if (initialConfigurationManager.isDefined) {
      configurationMananger = initialConfigurationManager.get
    }
    else {
      mediator ! Publish(WhoIsTopic, WhoIs(ConfigurationService))
    }

    if (initialDescriptorManager.isDefined) {
      descriptorMananger = initialDescriptorManager.get
    }
    else {
      mediator ! Publish(Topics.WhoIsTopic, WhoIs(DescriptorService))
    }

    log.info(s"BlueprintMaterializer started.")
    startPreparation()
  }

  override def receive: Receive = {

    case StartMaterializing =>

      blueprint match {
        case filterBlueprint: FilterBlueprint =>
          log.info(s"Preparing FilterBlueprint: ${filterBlueprint.ref.uuid}")
          configurationMananger ! GetConfigurationRequest(filterBlueprint.configuration)
          descriptorMananger ! GetDescriptorRequest(filterBlueprint.descriptor)

        case sourceBlueprint: SourceBlueprint =>
          log.info(s"Preparing SourceBlueprint: ${sourceBlueprint.ref.uuid}")
          configurationMananger ! GetConfigurationRequest(sourceBlueprint.configuration)
          descriptorMananger ! GetDescriptorRequest(sourceBlueprint.descriptor)

        case sinkBlueprint: SinkBlueprint =>
          log.info(s"Preparing SinkBlueprint: ${sinkBlueprint.ref.uuid}")
          configurationMananger ! GetConfigurationRequest(sinkBlueprint.configuration)
          descriptorMananger ! GetDescriptorRequest(sinkBlueprint.descriptor)
      }

      become(preparing(Preparation(blueprint)))

    case HereIam(DescriptorService, ref) =>
      log.info("BlueprintMat knows DescService")
      descriptorMananger = ref
      startPreparation()

    case HereIam(ConfigurationService, ref) =>
      configurationMananger = ref
      startPreparation()
  }

  private def preparing(preparation: Preparation): Receive = {
    case GetConfigurationResponse(configuration) =>
      log.debug(s"Got configuration: ${configuration.get}")
      becomeMaterializing(preparation.copy(configuration = configuration))

    case GetDescriptorResponse(descriptor) =>
      log.debug(s"Got descriptor: ${descriptor.get}")
      becomeMaterializing(preparation.copy(descriptor = descriptor))
  }


  private def materializing(materialization: Materialization): Receive = {
    case InstantiateStage =>
      blueprint match {
        case filterBlueprint: FilterBlueprint =>
          log.info(s"Creating FilterStage: ${filterBlueprint.ref.uuid}")
          filterManager ! CreateFilterStage(filterBlueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)

        case sourceBlueprint: SourceBlueprint =>
          log.info(s"Creating SourceStage: ${sourceBlueprint.ref.uuid}")
          filterManager ! CreateSourceStage(sourceBlueprint.ref, stageContext, materialization.descriptor.ref, materialization.configuration)

        case sinkBlueprint: SinkBlueprint =>
          log.info(s"Creating SinkStage: ${sinkBlueprint.ref.uuid}")
          filterManager ! CreateSinkStage(sinkBlueprint.ref,stageContext, materialization.descriptor.ref, materialization.configuration)
      }

    case sourceStageMessage: SourceStageCreated =>
      parent ! sourceStageMessage

    case filterStageMessage: FilterStageCreated =>
      parent ! filterStageMessage

    case sinkStageMessage: SinkStageCreated =>
      parent ! sinkStageMessage
  }

  private def becomeMaterializing(preparation: Preparation): Unit = {
    if (preparation.isComplete) {
      become(materializing(Materialization(preparation.blueprint, preparation.descriptor.get, preparation.configuration.get)), discardOld = true)
      self ! InstantiateStage
    }
    else {
      become(preparing(preparation), discardOld = true)
    }
  }

  private def startPreparation(): Unit = {
    if (descriptorMananger != null && configurationMananger != null) {
      self ! StartMaterializing
    }
  }

  case class Preparation(blueprint: SealedBlueprint, descriptor: Option[Descriptor] = None, configuration: Option[Configuration] = None) {
    def isComplete: Boolean = descriptor.isDefined && configuration.isDefined
  }

  case class Materialization(blueprint: SealedBlueprint, descriptor: Descriptor, configuration: Configuration)
}

package io.logbee.keyscore.agent.runtimes.jvm

import java.util.jar.Manifest

import akka.actor.{Actor, ActorLogging, Props}
import io.logbee.keyscore.agent.runtimes.StageLogicProvider._
import io.logbee.keyscore.agent.runtimes.jvm.ManifestStageLogicProvider.ManifestFinder
import io.logbee.keyscore.model.blueprint.BlueprintRef
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidToString
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import io.logbee.keyscore.model.util.Using.using
import io.logbee.keyscore.pipeline.api.LogicProviderFactory._
import io.logbee.keyscore.pipeline.api.stage._

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object ManifestStageLogicProvider {

  val EXTENSION_ATTRIBUTE = "keyscore-extensions"

  trait ManifestFinder {

    def find(): List[Manifest]
  }

  def apply(manifestFinder: ManifestFinder): Props = Props(new ManifestStageLogicProvider(manifestFinder))

  def apply(): Props = Props(new ManifestStageLogicProvider(() => {
    getClass.getClassLoader.getResources("META-INF/MANIFEST.MF").asScala
      .map(url => using(url.openStream())(stream => new java.util.jar.Manifest(stream)))
  }.toList))
}

class ManifestStageLogicProvider(manifestFinder: ManifestFinder) extends Actor with ActorLogging {

  import akka.actor.typed.scaladsl.adapter._
  import context.become

  val filterLoader = new FilterLoader()

  override def receive: Receive = {

    case Load(replyTo) =>

      val descriptors = manifestFinder.find()
        .flatMap(manifest => Option(manifest.getMainAttributes.getValue(ManifestStageLogicProvider.EXTENSION_ATTRIBUTE)))
        .flatMap(_.split(","))
        .map(_.trim())
        .flatMap(classname => Try(getClass.getClassLoader.loadClass(classname)) match {
          case Success(value) => Some(value)
          case Failure(exception) =>
            log.error(exception, s"Failed to load class: $classname")
            None
        })
        .flatMap(clazz => Try(filterLoader.loadDescriptors(clazz)) match {
          case Success(descriptor) => Some(descriptor.ref -> (descriptor -> clazz))
          case Failure(exception) =>
            log.error(exception, s"Failed to load descriptor for class: $clazz")
            None
        })
        .toMap

      replyTo ! LoadSuccess(descriptors.values.map(_._1).toList, self)

      become(loaded(descriptors))

    case _ => sender ! UninitializedFailure("Uninitialized")
  }

  private def loaded(descriptors: Map[DescriptorRef, (Descriptor, Class[_])]): Receive = {

    case CreateSourceStage(ref, parameters, replyTo) =>

      log.debug(s"Creating SourceStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          Try(new SourceStage(parameters, createSourceLogicProvider(clazz))) match {
            case Success(stage) =>
              replyTo ! SourceStageCreated(ref, stage, self)
            case Failure(exception) =>
              log.error(exception, "Failed to instantiate <{}> for SourceStage <{}>.", ref.uuid, parameters.uuid)
              replyTo ! StageCreationFailed(ref, BlueprintRef(parameters.uuid), self)
          }

        case None =>
          log.warning(s"Descriptor <{}> for Sourcestage <{}> not found.", ref.uuid, parameters.uuid)
          replyTo ! DescriptorNotFound(ref, BlueprintRef(parameters.uuid), self)
      }

    case CreateSinkStage(ref, parameters, replyTo) =>

      log.debug(s"Creating SinkStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          Try(new SinkStage(parameters, createSinkLogicProvider(clazz))) match {
            case Success(stage) =>
              replyTo ! SinkStageCreated(ref, stage, self)
            case Failure(exception) =>
              log.error(exception, "Failed to instantiate <{}> for SinkStage <{}>.", ref.uuid, parameters.uuid)
              replyTo ! StageCreationFailed(ref, BlueprintRef(parameters.uuid), self)
          }

        case None =>
          log.warning(s"Descriptor <{}> for SinkStage <{}> not found.", ref.uuid, parameters.uuid)
          replyTo ! DescriptorNotFound(ref, BlueprintRef(parameters.uuid), self)
      }

    case CreateFilterStage(ref, parameters, replyTo) =>

      log.debug(s"Creating FilterStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          Try(new FilterStage(parameters, createFilterLogicProvider(clazz))) match {
            case Success(stage) =>
              replyTo ! FilterStageCreated(ref, stage, self)
            case Failure(exception) =>
              log.error(exception, "Failed to instantiate <{}> for FilterStage <{}>.", ref.uuid, parameters.uuid)
              replyTo ! StageCreationFailed(ref, BlueprintRef(parameters.uuid), self)
          }

        case None =>
          log.warning(s"Descriptor <{}> for FilterStage <{}> not found.", ref.uuid, parameters.uuid)
          replyTo ! DescriptorNotFound(ref, BlueprintRef(parameters.uuid), self)
      }

    case CreateMergeStage(ref, parameters, replyTo) =>

      log.debug(s"Creating MergeStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          Try(new MergeStage(parameters, createMergeLogicProvider(clazz))) match {
            case Success(stage) =>
              replyTo ! MergeStageCreated(ref, stage, self)
            case Failure(exception) =>
              log.error(exception, "Failed to instantiate <{}> for MergeStage <{}>.", ref.uuid, parameters.uuid)
              replyTo ! StageCreationFailed(ref, BlueprintRef(parameters.uuid), self)
          }
        case None =>
          log.warning(s"Descriptor <{}> for Mergestage <{}> not found.", ref.uuid, parameters.uuid)
          replyTo ! DescriptorNotFound(ref, BlueprintRef(parameters.uuid), self)
      }

    case CreateBranchStage(ref, parameters, replyTo) =>

      log.debug(s"Creating BranchStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          Try(new BranchStage(parameters, createBranchLogicProvider(clazz))) match {
            case Success(stage) =>
              replyTo ! BranchStageCreated(ref, stage, self)
            case Failure(exception) =>
              log.error(exception, "Failed to instantiate <{}> for BranchStage <{}>.", ref.uuid, parameters.uuid)
              replyTo ! StageCreationFailed(ref, BlueprintRef(parameters.uuid), self)
          }
        case None =>
          log.warning(s"Descriptor <{}> for Branchstage <{}> not found.", ref.uuid, parameters.uuid)
          replyTo ! DescriptorNotFound(ref, BlueprintRef(parameters.uuid), self)
      }

    case _ =>
  }
}

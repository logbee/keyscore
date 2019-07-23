package io.logbee.keyscore.agent.pipeline.stage

import java.util.jar.Manifest

import akka.actor.{Actor, ActorLogging, Props}
import io.logbee.keyscore.agent.pipeline.FilterLoader
import io.logbee.keyscore.agent.pipeline.stage.ManifestStageLogicProvider.{EXTENSION_ATTRIBUTE, ManifestFinder}
import io.logbee.keyscore.agent.pipeline.stage.StageLogicProvider._
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import io.logbee.keyscore.model.util.Using.using
import io.logbee.keyscore.pipeline.api.LogicProviderFactory._
import io.logbee.keyscore.pipeline.api.stage.{BranchStage, FilterStage, MergeStage, SinkStage, SourceStage}

import scala.util.{Failure, Success, Try}
import scala.jdk.CollectionConverters._

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
        .flatMap(manifest => Option(manifest.getMainAttributes.getValue(EXTENSION_ATTRIBUTE)))
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
  }

  private def loaded(descriptors: Map[DescriptorRef, (Descriptor, Class[_])]): Receive = {

    case CreateSourceStage(ref, parameters, replyTo) =>
      log.debug(s"Creating SourceStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          val stage  = new SourceStage(parameters, createSourceLogicProvider(clazz))
          replyTo ! SourceStageCreated(ref, stage, self)

        case None => log.error(s"Failed to create SourceStage for: $ref")
      }

    case CreateSinkStage(ref, parameters, replyTo) =>
      log.debug(s"Creating SinkStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          val stage = new SinkStage(parameters, createSinkLogicProvider(clazz))
          replyTo ! SinkStageCreated(ref, stage, self)

        case None => log.error(s"Failed to create SinkStage for: $ref")
      }

    case CreateFilterStage(ref, parameters, replyTo) =>
      log.debug(s"Creating FilterStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          val stage = new FilterStage(parameters, createFilterLogicProvider(clazz))
          replyTo ! FilterStageCreated(ref, stage, self)

        case None => log.error(s"Failed to create FilterStage for: $ref")
      }

    case CreateMergeStage(ref, parameters, replyTo) =>
      log.debug(s"Creating MergeStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          val stage = new MergeStage(parameters, createMergeLogicProvider(clazz))
          replyTo ! MergeStageCreated(ref, stage, self)
        case None => log.error(s"Failed to create MergeStage for: $ref")
      }

    case CreateBranchStage(ref, parameters, replyTo) =>
      log.debug(s"Creating BranchStage: ${ref.uuid}")

      descriptors.get(ref) match {
        case Some((_, clazz)) =>
          val stage = new BranchStage(parameters, createBranchLogicProvider(clazz))
          replyTo ! BranchStageCreated(ref, stage, self)
        case None => log.error(s"Failed to create BranchStage for: $ref")
      }

    case _ =>
  }
}

package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import io.logbee.keyscore.agent.pipeline.Controller.{filterController, sourceController}
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor._
import io.logbee.keyscore.agent.pipeline.stage._
import io.logbee.keyscore.agent.pipeline.valve.ValveStage
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.model._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object PipelineSupervisor {

  case class CreatePipeline(configuration: PipelineConfiguration)

  case class StartPipeline(trials: Int)

  case class ConfigurePipeline(configurePipeline: PipelineConfiguration)

  private case class ControllerMaterialized(controller: Controller)

  private case class ControllerMaterializationFailed(cause: Throwable)

  def apply(filterManager: ActorRef) = Props(new PipelineSupervisor(filterManager))
}

/**
  * PipelineSupervisor
  *
  * States:       <br>
  * initial:      <br>
  * configuring:  <br>
  * materializing <br>
  * running:      <br>
  * <br>
  * Transitions:<br>
  * ''initial''   x CreatePipeline                  -> configuring                <br>
  * configuring   x SinkStageCreated                -> configuring                <br>
  * configuring   x SourceStageCreated              -> configuring                <br>
  * configuring   x FilterStageCreated              -> configuring                <br>
  * configuring   x StartPipeline                   -> [configuring|materializing]<br>
  * materializing x ControllerMaterialized          -> [materializing|running]    <br>
  * materializing x ControllerMaterializationFailed -> ''kill actor''             <br>
  * running       x ConfigurePipeline               -> configuring                <br>
  */
class PipelineSupervisor(filterManager: ActorRef) extends Actor with ActorLogging {

  import context.become

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val pipelineStartDelay = 5 seconds
  private val pipelineStartTrials = 3

  override def preStart(): Unit = {
    log.info(" StartUp complete.")
  }

  override def postStop(): Unit = {
    log.info(" Supervisor stopped.")
  }

  override def receive: Receive = {

    case CreatePipeline(pipelineConfiguration) =>

      log.info(s"Creating pipeline <${pipelineConfiguration.id}>.")

      val pipeline = Pipeline(pipelineConfiguration)
      val stageContext = StageContext(context.system, context.dispatcher)

      become(configuring(pipeline))

      log.info("Start sending messages to FilterManager ")

      filterManager ! CreateSinkStage(stageContext, pipelineConfiguration.sink)
      filterManager ! CreateSourceStage(stageContext, pipelineConfiguration.source)

      pipelineConfiguration.filter.foreach(filter => filterManager ! CreateFilterStage(stageContext, filter))

      scheduleStart(pipeline, pipelineStartTrials)

    case RequestPipelineInstance(receiver) =>
      receiver ! PipelineInstance(Red)
  }

  private def configuring(pipeline: Pipeline): Receive = {

    case SinkStageCreated(stage) =>
      log.info(s"Received SinkStage: $stage")
      become(configuring(pipeline.withSinkStage(stage)), discardOld = true)

    case SourceStageCreated(stage) =>
      log.info(s"Received SourceStage: $stage")
      become(configuring(pipeline.withSourceStage(stage)), discardOld = true)

    case FilterStageCreated(stage) =>
      log.info(s"Received FilterStage: $stage")
      become(configuring(pipeline.withFilterStage(stage)), discardOld = true)

    case StartPipeline(trials) =>

      if (trials <= 1) {
        log.error(s"Failed to start pipeline <${pipeline.id}> with ${pipeline.configuration}")
        context.stop(self)
      }
      else {

        if (pipeline.isComplete) {

          log.info(s"Constructing pipeline <${pipeline.configuration}>")

          val head = Source.fromGraph(pipeline.source.get).viaMat(new ValveStage) { (sourceProxyFuture, valveProxyFuture) =>
            val controller = for {
              sourceProxy <- sourceProxyFuture
              valveProxy <- valveProxyFuture
            } yield sourceController(sourceProxy, valveProxy)
            controller.onComplete(notifyControllerMaterialization)
            valveProxyFuture
          }
          val last = if (pipeline.filters.nonEmpty) {
            pipeline.filters.foldLeft(head) { (previousValve, filterStage) =>
              previousValve.viaMat(filterStage)(Keep.both).viaMat(new ValveStage) { (previous, outValveProxyFuture) =>
                previous match {
                  case (inValveProxyFuture, filterProxyFuture) =>
                    val controller = for {
                      inValveProxy <- inValveProxyFuture
                      filterProxy <- filterProxyFuture
                      outValveProxy <- outValveProxyFuture
                    } yield filterController(inValveProxy, filterProxy, outValveProxy)
                    controller.onComplete(notifyControllerMaterialization)
                    outValveProxyFuture
                }
              }
            }
          } else head
          val tail = last.toMat(pipeline.sink.get) { (valveProxyFuture, sinkProxyFuture) =>
            val controller = for {
              valveProxy <- valveProxyFuture
              sinkProxy <- sinkProxyFuture
            } yield Controller.sinkController(valveProxy, sinkProxy)
            controller.onComplete(notifyControllerMaterialization)
            sinkProxyFuture
          }

          become(materializing(pipeline, List.empty), discardOld = true)

          tail.run()
        }
        else {
          scheduleStart(pipeline, trials - 1)
        }
      }

    case RequestPipelineInstance(receiver) =>
      log.info("Received PipelineInstance Request")
      receiver ! PipelineInstance(pipeline.configuration.id, pipeline.configuration.name, pipeline.configuration.description, Red)

    case RequestPipelineConfigurations(receiver) =>
      log.info("Received PipelineConfiguration Request")
      receiver ! pipeline.configuration
  }

  private def materializing(pipeline: Pipeline, controllers: List[Controller]): Receive = {

    case ControllerMaterialized(controller) if controllers.size < pipeline.filters.size + 1 =>
      log.info(s"Controller <${controller.id}> has been materialized.")
      become(materializing(pipeline, controllers :+ controller), discardOld = true)

    case ControllerMaterialized(controller) =>
      log.info(s"Last Controller <${controller.id}> has been materialized.")
      become(running(new PipelineController(pipeline, controllers :+ controller)), discardOld = true)

    case ControllerMaterializationFailed(cause) =>
      log.error(message = s"Could not construct pipeline <${pipeline.id}> due to a failed materialization a controller!", cause = cause)
      context.stop(self)

    case RequestPipelineInstance(receiver) =>
      log.info("Received PipelineInstance Request")
      receiver ! PipelineInstance(pipeline.configuration.id, pipeline.configuration.name, pipeline.configuration.description, Yellow)

    case RequestPipelineConfigurations(receiver) =>
      log.info("Received PipelineConfiguration Request")
      receiver ! pipeline.configuration
  }

  private def running(controller: PipelineController): Receive = {

    case ConfigurePipeline(configuration) =>
      log.info(s"Updating pipeline <${configuration.id}>")

    case RequestPipelineInstance(receiver) =>
      log.info("Received PipelineInstance Request")
      receiver ! PipelineInstance(controller.configuration.id, controller.configuration.name, controller.configuration.description, Green)

    case RequestPipelineConfigurations(receiver) =>
      log.info("Received PipelineConfiguration Request")
      receiver ! controller.configuration

    case PauseFilter(filterId, doPause) =>
      val lastSender = sender
      controller.close(filterId, doPause).foreach(_.onComplete {
        case Success(state) => lastSender ! PauseFilterResponse(state)
        case Failure(e) => lastSender ! Failure
      })

    case DrainFilterValve(filterId, doDrain) =>
      val lastSender = sender
      controller.drain(filterId, doDrain).foreach(_.onComplete {
        case Success(state) => lastSender ! DrainFilterResponse(state)
        case Failure(e) => lastSender ! Failure
      })

    case InsertDatasets(filterId, datasets) =>
      val lastSender = sender
      controller.insert(filterId, datasets).foreach(_.onComplete {
        case Success(state) => lastSender ! InsertDatasetsResponse(state)
        case Failure(e) => lastSender ! Failure
      })

    case ExtractDatasets(filterId, amount) =>
      val lastSender = sender
      controller.extract(filterId, amount).foreach(_.onComplete {
        case Success(datasets) => lastSender ! ExtractDatasetsResponse(datasets)
        case Failure(e) => lastSender ! Failure
      })

    case ConfigureFilter(filterId, filterConfig) =>
      val lastSender = sender
      controller.configure(filterId, filterConfig).foreach(_.onComplete {
        case Success(state) => lastSender ! ConfigureFilterResponse(state)
        case Failure(e) => lastSender ! Failure
      })

    case CheckFilterState(filterId) =>
      val lastSender = sender
      controller.state(filterId).foreach(_.onComplete {
        case Success(state) =>
          lastSender ! CheckFilterStateResponse(state)
        case Failure(e) => lastSender ! Failure
      })
  }

  private def scheduleStart(pipeline: Pipeline, trials: Int): Unit = {
    if (trials > 0) {

      log.info(s"Scheduling start of pipeline <${pipeline.id}> in $pipelineStartDelay. (trials=$trials)")
      context.system.scheduler.scheduleOnce(pipelineStartDelay) {
        self ! StartPipeline(trials)
      }
    }
  }

  private def notifyControllerMaterialization(materialization: Try[Controller]): Unit = {
    materialization match {
      case Success(controller) =>
        self ! ControllerMaterialized(controller)
      case Failure(cause) =>
        self ! ControllerMaterializationFailed(cause)
    }
  }
}

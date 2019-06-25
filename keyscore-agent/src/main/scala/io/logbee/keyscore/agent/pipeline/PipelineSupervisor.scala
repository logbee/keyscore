package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor._
import io.logbee.keyscore.agent.pipeline.controller.Controller
import io.logbee.keyscore.agent.pipeline.controller.Controller.{filterController, sourceController}
import io.logbee.keyscore.agent.pipeline.valve.ValveStage
import io.logbee.keyscore.commons.cluster.Topics.MetricsTopic
import io.logbee.keyscore.commons.metrics.{ScrapeMetrics, ScrapeMetricsFailure, ScrapeMetricsSuccess}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import io.logbee.keyscore.model.data.Health.{Green, Red, Yellow}
import io.logbee.keyscore.pipeline.api.stage.StageContext

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object PipelineSupervisor {

  case class CreatePipeline(blueprint: PipelineBlueprint)

  case class StartPipeline(trials: Int)

  case class ConfigurePipeline(configurePipeline: PipelineConfiguration)

  private case class ControllerMaterialized(controller: Controller)

  private case class ControllerMaterializationFailed(cause: Throwable)

  def apply(filterManager: ActorRef) = Props(new PipelineSupervisor(filterManager))
}

/**
  * A '''PipelineSupervisor''' is responsible for only __one single Pipeline__. <br><br>
  * When he creates a Pipeline, he first collects all necessary `Stages` from each [[io.logbee.keyscore.agent.pipeline.BlueprintMaterializer]]. <br>
  * After the `PipelineSupervisor` has all Stages, he begins to __materialize__ the Stages to a Stream.<br><br>
  * When the Pipeline is running, he forwards all `Controller` requests to the `PipelineController`.
  *
  * @todo Add Logic to the `ConfigurePipeline` case.
  *
  *       <br><br>
  *       __States:__       <br>
  *       initial      <br>
  *       configuring  <br>
  *       materializing <br>
  *       running      <br>
  *       <br>
  *       __Transitions:__<br>
  *       ''initial''   x CreatePipeline                  -> configuring                <br>
  *       configuring   x SinkStageCreated                -> configuring                <br>
  *       configuring   x SourceStageCreated              -> configuring                <br>
  *       configuring   x FilterStageCreated              -> configuring                <br>
  *       configuring   x StartPipeline                   -> [configuring|materializing]<br>
  *       materializing x ControllerMaterialized          -> [materializing|running]    <br>
  *       materializing x ControllerMaterializationFailed -> ''kill actor''             <br>
  *       running       x ConfigurePipeline               -> configuring                <br>
  */
class PipelineSupervisor(filterManager: ActorRef) extends Actor with ActorLogging {

  import context.become

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val pipelineStartDelay = 5 seconds
  private val pipelineStartTrials = 3

  private var pipelineID: UUID = _

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    log.info(" started.")
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(MetricsTopic, self)
    log.info(" stopped.")
  }

  override def receive: Receive = {

    case CreatePipeline(pipelineBlueprint) =>

      val stageContext = StageContext(context.system, context.dispatcher)

      log.info(s"Creating pipeline <${pipelineBlueprint.ref.uuid}>.")

      val pipeline = Pipeline(pipelineBlueprint)
      pipelineID = pipeline.id

      become(configuring(pipeline))

      pipelineBlueprint.blueprints.foreach { stageBlueprint =>
        log.debug(s"Starting Materializer for blueprint: <${stageBlueprint.uuid}>")
        context.actorOf(BlueprintMaterializer(stageContext, stageBlueprint, filterManager))
      }

      scheduleStart(pipeline, pipelineStartTrials)

    case RequestPipelineInstance =>
      log.debug("Received RequestPipelineInstance")
      sender ! PipelineInstance(Red)
  }

  private def configuring(pipeline: Pipeline): Receive = {

    case SinkStageCreated(ref, stage) =>
      log.debug(s"Received SinkStage: $stage for <$ref>")
      become(configuring(pipeline.withSinkStage(ref, stage)), discardOld = true)

    case SourceStageCreated(ref, stage) =>
      log.debug(s"Received SourceStage: $stage for <$ref>")
      become(configuring(pipeline.withSourceStage(ref, stage)), discardOld = true)

    case FilterStageCreated(ref, stage) =>
      log.debug(s"Received FilterStage: $stage for <$ref>")
      become(configuring(pipeline.withFilterStage(ref, stage)), discardOld = true)

    case StartPipeline(trials) =>
      log.info(s"Received StartPipeline with $trials trials.")

      if (trials <= 1) {
        log.error(s"Failed to start pipeline <${pipeline.id}> with ${pipeline.pipelineBlueprint}")
        context.stop(self)
      }
      else {

        if (pipeline.isComplete) {

          log.info(s"Constructing pipeline: <${pipeline.pipelineBlueprint}>")

          val head = Source.fromGraph(pipeline.source.get).viaMat(new ValveStage) { (sourceProxyFuture, valveProxyFuture) =>
            val controller = for {
              sourceProxy <- sourceProxyFuture
              valveProxy <- valveProxyFuture
            } yield sourceController(sourceProxy, valveProxy)
            controller.onComplete(notifyControllerMaterialization)
            valveProxyFuture
          }
          val last = if (pipeline.filters.nonEmpty) {

            //TODO Dirty Quickfix for #58 | Implicits that the order of the BlueprintRefs must be correct
            val filterBlueprints = pipeline.pipelineBlueprint.blueprints.drop(1).dropRight(1)

            val pipelineFilters = filterBlueprints.map(key => pipeline.filters(key))

            pipelineFilters.foldLeft(head) { (previousValve, filterStage) =>
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
          log.debug("Pipeline wasn't complete. Scheduling start.")
          scheduleStart(pipeline, trials - 1)
        }
      }

    case RequestPipelineInstance =>
      log.debug("Received RequestPipelineInstance")
      sender ! PipelineInstance(pipeline.pipelineBlueprint.ref, pipeline.pipelineBlueprint.ref.uuid, pipeline.pipelineBlueprint.ref.uuid, Red)

    case RequestPipelineBlueprints(receiver) =>
      log.debug(s"Received RequestPipelineBlueprints from $receiver")
      receiver ! pipeline.pipelineBlueprint
  }

  private def materializing(pipeline: Pipeline, controllers: List[Controller]): Receive = {

    case ControllerMaterialized(controller) if controllers.size < pipeline.filters.size + 1 =>
      log.debug(s"Controller <${controller.id}> has been materialized.")
      become(materializing(pipeline, controllers :+ controller), discardOld = true)

    case ControllerMaterialized(controller) =>
      log.debug(s"Last Controller <${controller.id}> has been materialized.")

      mediator ! Subscribe(MetricsTopic, self)

      become(running(new PipelineController(pipeline, controllers :+ controller)(executionContext)), discardOld = true)

    case ControllerMaterializationFailed(cause) =>
      log.error(message = s"Could not construct pipeline <${pipeline.id}> due to a failed materialization a controller!", cause = cause)
      context.stop(self)

    case RequestPipelineInstance =>
      log.debug(s"Received Request for Pipeline Instance: <${pipeline.id}>")
      sender ! PipelineInstance(pipeline.pipelineBlueprint.ref, pipeline.pipelineBlueprint.ref.uuid, pipeline.pipelineBlueprint.ref.uuid, Yellow)

    case RequestPipelineBlueprints(receiver) =>
      log.debug(s"Received Request for Pipeline Blueprints: <${pipeline.id}>")
      receiver ! pipeline.pipelineBlueprint
  }

  private def running(controller: PipelineController): Receive = {

    case ConfigurePipeline(configuration) =>
      log.info(s"Updating pipeline <${configuration.id}>")

    case RequestPipelineInstance =>
      log.debug(s"<$pipelineID> Received Request for Pipeline Instance")
      sender ! PipelineInstance(controller.pipelineBlueprint.ref, controller.pipelineBlueprint.ref.uuid, controller.pipelineBlueprint.ref.uuid, Green)

    case RequestPipelineBlueprints(receiver) =>
      log.debug(s"<$pipelineID> Received Request for Pipeline Blueprints")
      receiver ! controller.pipelineBlueprint

    case PauseFilter(filterId, doPause) =>
      log.debug(s"<$pipelineID> Received PauseFilter($doPause) for filter <$filterId>")
      val _sender = sender
      controller.close(filterId, doPause).foreach(_.onComplete {
        case Success(state) => _sender ! PauseFilterResponse(state)
        case Failure(e) => _sender ! Failure(e)
      })

    case DrainFilterValve(filterId, doDrain) =>
      log.debug(s"<$pipelineID> Received DrainFilter($doDrain) for filter <$filterId>")
      val _sender = sender
      controller.drain(filterId, doDrain).foreach(_.onComplete {
        case Success(state) => _sender ! DrainFilterResponse(state)
        case Failure(e) => _sender ! Failure(e)
      })

    case InsertDatasets(filterId, datasets, where) =>
      log.debug(s"<$pipelineID> Received InsertDatasets with $datasets for filter <$filterId>")
      val _sender = sender
      controller.insert(filterId, datasets, where).foreach(_.onComplete {
        case Success(state) => _sender ! InsertDatasetsResponse(state)
        case Failure(e) => _sender ! Failure(e)
      })

    case ExtractDatasets(filterId, amount, where) =>
      log.debug(s"<$pipelineID> Received ExtractDatasets($amount) in for filter <$filterId>")
      val _sender = sender
      controller.extract(filterId, amount, where).foreach(_.onComplete {
        case Success(datasets) => _sender ! ExtractDatasetsResponse(datasets)
        case Failure(e) => _sender ! Failure(e)
      })

    case ConfigureFilter(filterId, filterConfig) =>
      log.debug(s"<$pipelineID> Received ConfigureFilter with $filterConfig for filter <$filterId>")
      val _sender = sender
      controller.configure(filterId, filterConfig).foreach(_.onComplete {
        case Success(state) => _sender ! ConfigureFilterResponse(state)
        case Failure(e) => _sender ! Failure(e)
      })

    case CheckFilterState(filterId) =>
      log.debug(s"<$pipelineID> Received CheckFilterState for filter <$filterId>")
      val _sender = sender
      controller.state(filterId).foreach(_.onComplete {
        case Success(state) => _sender ! CheckFilterStateResponse(state)
        case Failure(e) => _sender ! Failure(e)
      })

    case ClearBuffer(filterId) =>
      log.debug(s"<$pipelineID> Received ClearBuffer for filter <$filterId>")
      val _sender = sender
      controller.clear(filterId).foreach(_.onComplete {
        case Success(state) => _sender ! ClearBufferResponse(state)
        case Failure(e) => _sender ! Failure(e)
      })

    case ScrapeMetrics(manager) =>
      controller.scrapePipeline().onComplete {
        case Success(metrics) =>
          manager ! ScrapeMetricsSuccess(metrics.map { case (id, collection) => id.toString -> collection })
        case Failure(e) =>
          manager ! ScrapeMetricsFailure(pipelineID.toString, e.getMessage)
      }
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

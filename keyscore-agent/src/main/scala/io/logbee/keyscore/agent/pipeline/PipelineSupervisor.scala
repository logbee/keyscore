package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.Controller.{filterController, sourceController}
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor.{ConfigurePipeline, CreatePipeline, RequestPipelineState, StartPipeline}
import io.logbee.keyscore.agent.pipeline.stage._
import io.logbee.keyscore.model.{Health, PipelineConfiguration, PipelineState}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future.sequence
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.language.postfixOps

object PipelineSupervisor {

  case class CreatePipeline(configuration: PipelineConfiguration)

  case class StartPipeline(trials: Int)

  case class ConfigurePipeline(configurePipeline: PipelineConfiguration)

  case object RequestPipelineState

  def apply(filterManager: ActorRef) = Props(new PipelineSupervisor(filterManager))
}

/**
  * PipelineSupervisor
  *
  * States:
  * initial:
  * configuring:
  * running:
  *
  * Transitions:
  * ''initial'' x CreatePipeline      -> configuring
  * configuring x SinkStageCreated    -> configuring
  * configuring x SourceStageCreated  -> configuring
  * configuring x FilterStageCreated  -> configuring
  * configuring x StartPipeline       -> [configuring|running]
  * running     x ConfigurePipeline   -> configuring
  */
class PipelineSupervisor(filterManager: ActorRef) extends Actor with ActorLogging {

  import context.become

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val timeout: Timeout = 5 seconds
  private val pipelineStartDelay = 5 seconds

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

      scheduleStart(pipeline, 3)

    case RequestPipelineState =>
      sender ! PipelineState(Health.Red)
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

          log.info(s"Starting Pipeline <${pipeline.configuration}>")

          val controllerFutures: ListBuffer[Future[Controller]] = ListBuffer.empty

          val head = Source.fromGraph(pipeline.source.get).viaMat(new ValveStage) { (sourceProxyFuture, valveProxyFuture) =>
            val controller = for {
              sourceProxy <- sourceProxyFuture
              valveProxy <- valveProxyFuture
            } yield sourceController(sourceProxy, valveProxy)
            controllerFutures += controller
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
                    controllerFutures += controller
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
            controllerFutures += controller
            sinkProxyFuture
          }

          tail.run()

          // TODO: Find a better alternative to implement this behaviour!
          val controllerListFuture = sequence(controllerFutures.map(_.map(Some(_)).fallbackTo(Future(None))))
          val controllers = Await.result(controllerListFuture, 15 seconds)
          val controller = new PipelineController(pipeline, List.empty)

          become(running(controller))

          log.info(s"Started pipeline <${pipeline.id}>.")
        }
        else {
          scheduleStart(pipeline, trials - 1)
        }
      }

    case RequestPipelineState =>
      log.info("Received PipelineState Request")
      sender ! PipelineState(pipeline.configuration, Health.Yellow)
  }

  private def running(controller: PipelineController): Receive = {

    case ConfigurePipeline(configuration) =>
      log.info(s"Updating pipeline <${configuration.id}>")

    case RequestPipelineState =>
      log.info("Received PipelineState Request")
      sender ! PipelineState(controller.configuration, Health.Green)
  }

  private def scheduleStart(pipeline: Pipeline, trials: Int): Unit = {
    if (trials > 0) {

      log.info(s"Scheduling start of pipeline <${pipeline.id}> in $pipelineStartDelay. (trials=$trials)")
      context.system.scheduler.scheduleOnce(pipelineStartDelay) {
        self ! StartPipeline(trials)
      }
    }
  }
}

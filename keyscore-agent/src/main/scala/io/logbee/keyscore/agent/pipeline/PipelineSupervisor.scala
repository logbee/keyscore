package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor.{ConfigurePipeline, CreatePipeline, RequestPipelineState, StartPipeline}
import io.logbee.keyscore.agent.pipeline.stage.{StageContext, ValveProxy, ValveStage, ValveState}
import io.logbee.keyscore.model.filter.FilterProxy
import io.logbee.keyscore.model.sink.SinkProxy
import io.logbee.keyscore.model.source.SourceProxy
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
  * ''initial'' x CreatePipeline        -> configuring
  * configuring x SinkStageCreated    -> configuring
  * configuring x SourceStageCreated  -> configuring
  * configuring x FilterStageCreated  -> configuring
  * configuring x StartPipeline         -> [configuring|running]
  * running     x ConfigurePipeline     -> configuring
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
      log.info("Received PipelineState Request")
      sender ! PipelineState(Health.Red)
  }

  private def configuring(pipeline: Pipeline): Receive = {

    case SinkStageCreated(stage) =>
      log.info(s"Received SinkStage: $stage")
      become(configuring(pipeline.withSink(stage)), discardOld = true)

    case SourceStageCreated(stage) =>
      log.info(s"Received SourceStage: $stage")
      become(configuring(pipeline.withSource(stage)), discardOld = true)

    case FilterStageCreated(stage) =>
      log.info(s"Received FilterStage: $stage")
      become(configuring(pipeline.withFilter(stage)), discardOld = true)
      

    case StartPipeline(trials) =>

      if (trials <= 1) {
        log.error(s"Failed to start pipeline <${pipeline.id}> with ${pipeline.configuration}")
        context.stop(self)
      }
      else {

        if (pipeline.isComplete) {

          log.info(s"Starting Pipeline <${pipeline.configuration}>")

          val filters = pipeline.filters.foldLeft(ListBuffer.empty) {(list, filter) =>
            list :+ new ValveStage()
            list :+ filter
            list
          }

          filters :+ new ValveStage()

          val filterProxies = new ListBuffer[Future[FilterProxy]]

          val head = Source.fromGraph(pipeline.source.get)
          val last = if (filters.nonEmpty) {
            filters.foldLeft(head) { (current, next) =>
              current.viaMat(next) { (currentFuture, nextFuture) =>
                filterProxies += nextFuture
                currentFuture
              }
            }
          } else head

          val tail = last.toMat(pipeline.sink.get)(Keep.both)
          val (sourceProxyFuture, sinkProxyFuture) = tail.run()

          val filterListFuture = sequence(filterProxies.map(_.map(Some(_)).fallbackTo(Future(None))))

          val pipelineFutures = for {
            source <- sourceProxyFuture.mapTo[SourceProxy]
            sink <- sinkProxyFuture.mapTo[SinkProxy]
            filters <- filterListFuture
          } yield (source, sink, filters)

          Await.ready(pipelineFutures, 10 seconds)

          become(running(pipeline))

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

  private def running(pipeline: Pipeline): Receive = {

    case ConfigurePipeline(configuration) =>
      log.info(s"Updating pipeline <${configuration.id}>")

    case RequestPipelineState =>
      log.info("Received PipelineState Request")
      sender ! PipelineState(pipeline.configuration, Health.Green)
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

package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor.{ConfigurePipeline, CreatePipeline, RequestPipelineState, StartPipeline}
import io.logbee.keyscore.agent.pipeline.stage.StageContext
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

  case class ConfigurePipeline(configureStream: PipelineConfiguration)

  case object RequestPipelineState

  def apply(filterManager: ActorRef) = Props(new PipelineSupervisor(filterManager))
}

/**
  * StreamSupervisor
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
  private val streamStartDelay = 5 seconds

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def receive: Receive = {

    case CreatePipeline(streamConfiguration) =>

      log.info(s"Crng pipeline <${streamConfiguration.id}>.")

      val stream = Pipeline(streamConfiguration)
      val stageContext = StageContext(context.system, context.dispatcher)

      become(configuring(stream))

      log.info("[Pipelinesupervisor]: Start sending messages to Filtermanager ")

      filterManager ! CreateSinkStage(stageContext, streamConfiguration.sink)
      filterManager ! CreateSourceStage(stageContext, streamConfiguration.source)

      streamConfiguration.filter.foreach(filter => filterManager ! CreateFilterStage(stageContext, filter))

      scheduleStart(stream, 3)

    case RequestPipelineState =>
      sender ! PipelineState(null, Health.Red)
  }

  private def configuring(stream: Pipeline): Receive = {

    case SinkStageCreated(stage) =>
      log.info(s"Received SinkStage: $stage")
      become(configuring(stream.withSink(stage)), discardOld = true)

    case SourceStageCreated(stage) =>
      log.info(s"Received SourceStage: $stage")
      become(configuring(stream.withSource(stage)), discardOld = true)

    case FilterStageCreated(stage) =>
      log.info(s"Received FilterStage: $stage")
      become(configuring(stream.withFilter(stage)), discardOld = true)

    case StartPipeline(trials) =>

      if (trials <= 1) {
        log.error(s"Failed to start pipeline <${stream.id}> with ${stream.configuration}")
        context.stop(self)
      }
      else {

        if (stream.isComplete) {

          log.info(s"Starting Stream <${stream.configuration}>")

          val filterProxies = new ListBuffer[Future[FilterProxy]]

          val head = Source.fromGraph(stream.source.get)
          val last = if (stream.filters.nonEmpty) {
            stream.filters.foldLeft(head) { (current, next) =>
              current.viaMat(next) { (currentFuture, nextFuture) =>
                filterProxies += nextFuture
                currentFuture
              }
            }
          } else head

          val tail = last.toMat(stream.sink.get)(Keep.both)
          val (sourceProxyFuture, sinkProxyFuture) = tail.run()

          val filterListFuture = sequence(filterProxies.map(_.map(Some(_)).fallbackTo(Future(None))))

          val streamFutures = for {
            source <- sourceProxyFuture.mapTo[SourceProxy]
            sink <- sinkProxyFuture.mapTo[SinkProxy]
            filters <- filterListFuture
          } yield (source, sink, filters)

          Await.ready(streamFutures, 10 seconds)

          become(running(stream))

          log.info(s"Started pipeline <${stream.id}>.")
        }
        else {
          scheduleStart(stream, trials - 1)
        }
      }

    case RequestPipelineState =>
      sender ! PipelineState(stream.configuration, Health.Yellow)
  }

  private def running(Stream: Pipeline): Receive = {

    case ConfigurePipeline(configuration) =>
      log.info(s"Updating pipeline <${configuration.id}>")

    case RequestPipelineState =>
      sender ! PipelineState(Stream.configuration, Health.Green)
  }

  private def scheduleStart(Stream: Pipeline, trials: Int): Unit = {
    if (trials > 0) {

      log.info(s"Scheduling start of pipeline <${Stream.id}> in $streamStartDelay. (trails=$trials)")
      context.system.scheduler.scheduleOnce(streamStartDelay) {
        self ! StartPipeline(trials)
      }
    }
  }
}

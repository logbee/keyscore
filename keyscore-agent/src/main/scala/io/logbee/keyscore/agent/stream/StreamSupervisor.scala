package io.logbee.keyscore.agent.stream

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.FilterManager._
import io.logbee.keyscore.agent.stream.StreamSupervisor.{ConfigureStream, CreateStream, RequestStreamState, StartStream}
import io.logbee.keyscore.agent.stream.stage.StageContext
import io.logbee.keyscore.model.filter.FilterProxy
import io.logbee.keyscore.model.sink.SinkProxy
import io.logbee.keyscore.model.source.SourceProxy
import io.logbee.keyscore.model.{Health, StreamConfiguration, StreamState}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future.sequence
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.language.postfixOps

object StreamSupervisor {

  case class CreateStream(configuration: StreamConfiguration)

  case class StartStream(trials: Int)

  case class ConfigureStream(configureStream: StreamConfiguration)

  case object RequestStreamState

  def apply(filterManager: ActorRef) = Props(new StreamSupervisor(filterManager))
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
  * ''initial'' x CreateStream        -> configuring
  * configuring x SinkStageCreated    -> configuring
  * configuring x SourceStageCreated  -> configuring
  * configuring x FilterStageCreated  -> configuring
  * configuring x StartStream         -> [configuring|running]
  * running     x ConfigureStream     -> configuring
  */
class StreamSupervisor(filterManager: ActorRef) extends Actor with ActorLogging {

  import context.become

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val timeout: Timeout = 5 seconds
  private val streamStartDelay = 5 seconds

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def receive: Receive = {

    case CreateStream(streamConfiguration) =>

      log.info(s"Creating stream <${streamConfiguration.id}>.")

      val stream = Stream(streamConfiguration)
      val stageContext = StageContext(context.system, context.dispatcher)

      become(configuring(stream))

      filterManager ! CreateSinkStage(stageContext, streamConfiguration.sink)
      filterManager ! CreateSourceStage(stageContext, streamConfiguration.source)

      streamConfiguration.filter.foreach(filter => filterManager ! CreateFilterStage(stageContext, filter))

      scheduleStart(stream, 3)

    case RequestStreamState =>
      sender ! StreamState(null, Health.Red)
  }

  private def configuring(stream: Stream): Receive = {

    case SinkStageCreated(stage) =>
      log.info(s"Received SinkStage: $stage")
      become(configuring(stream.withSink(stage)), discardOld = true)

    case SourceStageCreated(stage) =>
      log.info(s"Received SourceStage: $stage")
      become(configuring(stream.withSource(stage)), discardOld = true)

    case FilterStageCreated(stage) =>
      log.info(s"Received FilterStage: $stage")
      become(configuring(stream.withFilter(stage)), discardOld = true)

    case StartStream(trials) =>

      if (trials <= 1) {
        log.error(s"Failed to start stream <${stream.id}> with ${stream.configuration}")
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

          log.info(s"Started stream <${stream.id}>.")
        }
        else {
          scheduleStart(stream, trials - 1)
        }
      }

    case RequestStreamState =>
      sender ! StreamState(stream.configuration, Health.Yellow)
  }

  private def running(Stream: Stream): Receive = {

    case ConfigureStream(configuration) =>
      log.info(s"Updating stream <${configuration.id}>")

    case RequestStreamState =>
      sender ! StreamState(Stream.configuration, Health.Green)
  }

  private def scheduleStart(Stream: Stream, trials: Int): Unit = {
    if (trials > 0) {

      log.info(s"Scheduling start of stream <${Stream.id}> in $streamStartDelay. (trails=$trials)")
      context.system.scheduler.scheduleOnce(streamStartDelay) {
        self ! StartStream(trials)
      }
    }
  }
}

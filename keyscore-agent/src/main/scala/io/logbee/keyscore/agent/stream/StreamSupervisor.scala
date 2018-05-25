package io.logbee.keyscore.agent.stream

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.FilterManager._
import io.logbee.keyscore.agent.stream.StreamSupervisor.{ConfigureStream, CreateStream, RequestStreamState, StartStream}
import io.logbee.keyscore.agent.stream.stage.{FilterStage, SinkStage, SourceStage, StageContext}
import io.logbee.keyscore.model.filter.FilterProxy
import io.logbee.keyscore.model.sink.SinkProxy
import io.logbee.keyscore.model.source.SourceProxy
import io.logbee.keyscore.model.{Health, StreamConfiguration, StreamState}

import scala.collection.mutable.ListBuffer
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

class StreamSupervisor(filterManager: ActorRef) extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val timeout: Timeout = 5 seconds
  private val streamStartDelay = 5 seconds

  private var streamConfiguration: StreamConfiguration = _

  private var sinkStage: Option[SinkStage] = None
  private var sourceStage: Option[SourceStage] = None
  private val filterStages: ListBuffer[Option[FilterStage]] = ListBuffer.empty

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def receive: Receive = {

    case CreateStream(configuration) =>
      log.info(s"Creating stream <${configuration.id}>.")

      streamConfiguration = configuration

      val stageContext = StageContext(context.system, context.dispatcher)

      filterManager ! CreateSinkStage(stageContext, configuration.sink)
      filterManager ! CreateSourceStage(stageContext, configuration.source)

      configuration.filter.foreach(filter => filterManager ! CreateFilterStage(stageContext, filter))

      scheduleStart(3)

    case SinkStageCreated(stage) =>
      log.info(s"Received SinkStage: $stage")
      sinkStage = Option(stage)

    case SourceStageCreated(stage) =>
      log.info(s"Received SourceStage: $stage")
      sourceStage = Option(stage)

    case FilterStageCreated(stage) =>
      log.info(s"Received FilterStage: $stage")
      filterStages.append(Option(stage))

    case StartStream(trials) =>

      if (trials <= 1) {
        log.error(s"Failed to start stream <${streamConfiguration.id}> with $streamConfiguration")
        context.stop(self)
      }
      else {

        if (stagesReady) {

          log.info(s"Starting Stream <$streamConfiguration>")

          val filterProxies = new ListBuffer[Future[FilterProxy]]

          val head = Source.fromGraph(sourceStage.get)
          val last = if (filterStages.nonEmpty) {
            filterStages.map(_.get).foldLeft(head) { (current, next) =>
              current.viaMat(next) { (currentFuture, nextFuture) =>
                filterProxies += nextFuture
                currentFuture
              }
            }
          } else head

          val tail = last.toMat(sinkStage.get)(Keep.both)
          val (sourceProxyFuture, sinkProxyFuture) = tail.run()

          val filterFutureList = Future.sequence(filterProxies.map(_.map(Some(_)).fallbackTo(Future(None))))

          val streamFutures = for {
            source <- sourceProxyFuture.mapTo[SourceProxy]
            sink <- sinkProxyFuture.mapTo[SinkProxy]
            filters <- filterFutureList
          } yield (source, sink, filters)

          Await.ready(streamFutures, 10 seconds)

          log.info(s"Started stream <${streamConfiguration.id}>.")
        }
        else {
          scheduleStart(trials - 1)
        }
      }

    case ConfigureStream(configuration) =>
      log.info(s"Updating stream <${configuration.id}>")

    case RequestStreamState =>
      sender ! StreamState(streamConfiguration, Health.Green)

    case _ =>
  }

  private def scheduleStart(trials: Int): Unit = {
    if (trials > 0) {

      log.info(s"Scheduling start of stream <${streamConfiguration.id}> in $streamStartDelay. (trails=$trials)")
      context.system.scheduler.scheduleOnce(streamStartDelay) {
        self ! StartStream(trials)
      }
    }
  }

  private def stagesReady: Boolean = {
    sinkStage.isDefined && sourceStage.isDefined
  }
}

/*
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.management.FilterManager.{GraphBuildException, GraphBuildingAnswer, GraphBuilt}
import io.logbee.keyscore.agent.stream.StreamSupervisor.{ChangeStream, CreateStream, ShutdownStream, StreamCreationError}
import io.logbee.keyscore.commons.cluster.{CreateNewStream, GraphBuildingException, GraphCreated, StreamKilled}
import io.logbee.keyscore.model.StreamModel

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object StreamSupervisor {
  def props(filterManager: ActorRef)(implicit materializer: ActorMaterializer): Props = {
    Props(new StreamSupervisor(filterManager))
  }

  case class CreateStream(streamId: UUID, streamSpec: StreamModel)

  case class StreamCreationError(streamId: UUID, errorMsg: String)
  case object StreamCreated

  case class ChangeStream(streamId: UUID, streamSpec: StreamModel)

  case class StreamSupervisorError(errorMsg: String)

  case class ShutdownStream(streamId: UUID)
}

class StreamSupervisor(filterManager: ActorRef)
                      (implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 30 seconds

  private val streams = mutable.HashMap.empty[UUID, UniqueKillSwitch]

  override def preStart(): Unit = {
    log.info("Started StreamSupervisor.")
  }

  override def postStop(): Unit = {
    log.debug("Stopped StreamSupervisor.")
  }

  override def receive: Receive = {

    case CreateNewStream(streamId, streamSpec) =>
      log.info("Creating stream with id: "+streamId)
      val streamManager = sender()
      val future: Future[GraphBuildingAnswer] = ask(filterManager, CreateStream(streamId, streamSpec)).mapTo[GraphBuildingAnswer]

      // a useful timeout duration should be evaluated
      val graphAnswer = Await.result(future, 30 seconds)

      val graph = graphAnswer.answer match {
        case Some(builtGraph: GraphBuilt) =>
          streamManager ! GraphCreated(builtGraph.streamID)
          Some(builtGraph.graph)
        case Some(e: GraphBuildException) =>
          streamManager ! GraphBuildingException(e.streamID, e.streamSpec, e.errorMsg)
          None
        case _ =>
          streamManager ! GraphBuildException(streamId, streamSpec, "Unknown error occurred while building the stream")
          None
      }

      log.info("Running graph with id: " + streamId)

      if (graph.isDefined) {
        var killSwitch = graph.get.run(materializer)
        streams.put(streamId, killSwitch)
      }

    case StreamCreationError(streamId, errorMsg) =>
      log.info("Stream creation failed for stream with id: " + streamId + " and error message:\n" + errorMsg)

    case ChangeStream(streamId, streamSpec) =>
      log.info("Updating stream with id: "+streamId)
    //      var actorToChange = streams.get(UUID)

    case ShutdownStream(streamID) =>
      log.info("Shutting down stream with id: " + streamID)
      var streamToKill = streams.get(streamID)
      streamToKill.get.shutdown()
      streams.remove(streamID)
      log.info("Shutdown stream with id: " + streamID)
      sender ! StreamKilled(streamID)
  }
}
*/
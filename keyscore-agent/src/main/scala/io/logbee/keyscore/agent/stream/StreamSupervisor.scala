package io.logbee.keyscore.agent.stream

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.FilterManager._
import io.logbee.keyscore.agent.stream.StreamSupervisor.{ConfigureStream, CreateStream, RequestStreamState, StartStream}
import io.logbee.keyscore.agent.stream.stage.{FilterStage, SinkStage, SourceStage, StageContext}
import io.logbee.keyscore.model.Health.Value
import io.logbee.keyscore.model.{Health, StreamConfiguration, StreamState}

import scala.concurrent.duration._
import scala.language.postfixOps

object StreamSupervisor {

  case class CreateStream(configuration: StreamConfiguration)

  case object StartStream

  case class ConfigureStream(configureStream: StreamConfiguration)

  case object RequestStreamState

  def apply(filterManager: ActorRef) = Props(new StreamSupervisor(filterManager))
}

class StreamSupervisor(filterManager: ActorRef) extends Actor with ActorLogging {

  import context._

  private val timeout: Timeout = 5 seconds

  private var streamConfiguration: StreamConfiguration = _

  private var sinkStage: Option[SinkStage] = None
  private var sourceStage: Option[SourceStage] = None
  private var filterStages: List[Option[FilterStage]] = List.empty

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def receive: Receive = {

    case CreateStream(configuration) =>
      log.info(s"Creating stream: ${configuration.id}")

      streamConfiguration
      val stageContext = StageContext(system, dispatcher)



      filterManager ! CreateSinkStage(stageContext, configuration.sink)
      filterManager ! CreateSourceStage(stageContext,configuration.source)
      configuration.filter.foreach(filter => filterManager ! CreateFilterStage(stageContext, filter))

      system.scheduler.scheduleOnce(10 seconds) {
        self ! StartStream
      }

    case SinkStageCreated(stage) =>
      sinkStage = Option(stage)

    case SourceStageCreated(stage) =>
      sourceStage = Option(stage)

    case StartStream =>
      log.info(s"Start Stream: sink(${sinkStage.isDefined}), source(${sourceStage.isDefined})")

    case ConfigureStream(configuration) =>
      log.info(s"Updating stream: ${configuration.id}")

    case RequestStreamState =>
      sender ! StreamState(streamConfiguration, Health.Green)

    case _ =>
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
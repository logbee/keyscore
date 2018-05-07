package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import io.logbee.keyscore.agent.FilterManager.{GraphBuildException, GraphBuildingAnswer, GraphBuilt}
import io.logbee.keyscore.agent.stream.StreamSupervisor._
import io.logbee.keyscore.commons.cluster.{CreateNewStream, GraphBuildingException, StreamKilled}
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
          streamManager ! StreamCreated(builtGraph.streamID)
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

    case ShutdownStream(streamId) =>
      log.info("Shutting down stream with id: "+streamId)
      var streamToKill = streams.get(UUID)
      streamToKill.get.shutdown()
      streams.remove(UUID)
      log.info("Shutdown stream with id: " + streamId)
      sender ! StreamKilled(streamId)
  }
}

package streammanagement

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.stream.scaladsl.RunnableGraph
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import io.logbee.keyscore.frontier.stream.StreamManager.{CreateNewStream, StreamCreatedWithID}
import io.logbee.keyscore.model.StreamModel
import streammanagement.FilterManager.{BuildGraph, BuildGraphAnswerWrapper, BuildGraphException, BuiltGraph}
import streammanagement.StreamSupervisor.ShutdownGraph

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object StreamSupervisor {

  def props(filterManager: ActorRef)(implicit materializer: ActorMaterializer): Props = {
    Props(new StreamSupervisor(filterManager))
  }

  case object RunStream

  case object ShutdownGraph

}

class StreamSupervisor(filterManager: ActorRef)
                      (implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 2 seconds


  var killSwitch: Option[UniqueKillSwitch] = None

  override def preStart(): Unit = {
    log.info("Starting StreamActor ")
  }

  override def postStop(): Unit = {
    log.debug("Stopping StreamActor")
  }

  override def receive = {
    case ShutdownGraph =>
      killSwitch.get.shutdown()
      log.debug("Shutdown Graph")
      context.stop(self)
    case CreateNewStream(streamId, stream) =>
      val future: Future[BuildGraphAnswerWrapper] = ask(filterManager, BuildGraph(streamId, stream)).mapTo[BuildGraphAnswerWrapper]

      val graphAnswer = Await.result(future, 2 seconds)

      val graph = graphAnswer.answer match {
        case Some(builtGraph: BuiltGraph) =>
          sender ! StreamCreatedWithID(builtGraph.streamId)
          Some(builtGraph.graph)
        case Some(buildGraphException: BuildGraphException) =>
          sender ! buildGraphException
          None
        case _ =>
          sender ! BuildGraphException(streamId, stream, "Unknown error occured while building the stream")
          None
      }

      log.info("running graph")

      if (graph.isDefined) killSwitch = Some(graph.get.run())


  }
}

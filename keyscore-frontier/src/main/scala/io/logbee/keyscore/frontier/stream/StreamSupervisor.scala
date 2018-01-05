package streammanagement

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.stream.scaladsl.RunnableGraph
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import io.logbee.keyscore.model.StreamModel
import streammanagement.FilterManager.{BuildGraph, BuiltGraph}
import streammanagement.StreamSupervisor.ShutdownGraph

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object StreamSupervisor {

  def props(streamID: UUID, stream: StreamModel,filterManager:ActorRef)(implicit materializer: ActorMaterializer): Props = {
    Props(new StreamSupervisor(streamID,stream,filterManager))
  }

  case object RunStream

  case object ShutdownGraph

}

class StreamSupervisor(streamId: UUID, stream: StreamModel, filterManager:ActorRef)
                      (implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 2 seconds

  val future: Future[BuiltGraph] = ask(filterManager, BuildGraph(streamId,stream)).mapTo[BuiltGraph]
  val graph: RunnableGraph[UniqueKillSwitch] = Await.result(future, 2 seconds).graph

  log.info("running graph")
  var killSwitch: UniqueKillSwitch = graph.run()

  override def preStart(): Unit = {
    log.info("Starting StreamActor ")
  }

  override def postStop(): Unit = {
    log.debug("Stopping StreamActor")
  }

  override def receive = {
    case ShutdownGraph =>
      killSwitch.shutdown()
      log.debug("Shutdown Graph")
      context.stop(self)
  }
}

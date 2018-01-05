package streammanagement

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import io.logbee.keyscore.frontier.filters.{CommittableEvent, FilterHandle}
import io.logbee.keyscore.model.StreamModel
import streammanagement.GraphBuilderActor.{BuildGraph, BuiltGraph, PrintList}
import streammanagement.RunningStreamActor.ShutdownGraph

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object RunningStreamActor {

  def props(streamID: UUID, stream: StreamModel,filterManager:ActorRef)(implicit materializer: ActorMaterializer): Props = {
    Props(new RunningStreamActor(streamID,stream,filterManager))
  }

  case object RunStream

  case object ShutdownGraph

}

class RunningStreamActor(streamId: UUID, stream: StreamModel,filterManager:ActorRef)
                        (implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  /*
    private implicit val executionContext: ExecutionContext = context.system.dispatcher
  */
  implicit val timeout: Timeout = 2 seconds



  val future: Future[BuiltGraph] = ask(filterManager, BuildGraph(streamId,stream)).mapTo[BuiltGraph]
  val graph: RunnableGraph[UniqueKillSwitch] = Await.result(future, 2 seconds).graph

  log.info("running graph")
  var killSwitch: UniqueKillSwitch = graph.run()

  filterManager ! PrintList

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

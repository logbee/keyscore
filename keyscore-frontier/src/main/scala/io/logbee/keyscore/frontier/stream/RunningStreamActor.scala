package streammanagement

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import io.logbee.keyscore.frontier.filters.{CommittableEvent, FilterHandle}
import streammanagement.GraphBuilderActor.{BuildGraph, BuiltGraph}
import streammanagement.RunningStreamActor.ShutdownGraph

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object RunningStreamActor {

  def props(
             source: Source[CommittableEvent, UniqueKillSwitch],
             sink: Sink[CommittableEvent, NotUsed],
             flows: List[Flow[CommittableEvent, CommittableEvent,Future[FilterHandle]]]
           )
           (implicit materializer: ActorMaterializer): Props = {
    Props(new RunningStreamActor(source, sink, flows))
  }

  case object RunStream

  case object ShutdownGraph

}

class RunningStreamActor(
                          source: Source[CommittableEvent, UniqueKillSwitch],
                          sink: Sink[CommittableEvent, NotUsed],
                          flows: List[Flow[CommittableEvent, CommittableEvent,Future[FilterHandle]]]
                        )(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  /*
    private implicit val executionContext: ExecutionContext = context.system.dispatcher
  */
  implicit val timeout: Timeout = 2 seconds

  val graphBuilderActor: ActorRef = context.actorOf(GraphBuilderActor.props())

  val future: Future[BuiltGraph] = ask(graphBuilderActor, BuildGraph(source, sink, flows)).mapTo[BuiltGraph]
  val graph: RunnableGraph[UniqueKillSwitch] = Await.result(future, 2 seconds).graph

  log.debug("running graph")
  var killSwitch: UniqueKillSwitch = graph.run()



  override def preStart(): Unit = {
    log.debug("Starting StreamActor ")
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

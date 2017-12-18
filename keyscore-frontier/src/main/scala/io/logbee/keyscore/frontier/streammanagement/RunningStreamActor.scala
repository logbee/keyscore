package streammanagement

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.kafka.scaladsl.Consumer
import akka.pattern.ask
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import akka.util.Timeout
import filter.CommitableFilterMessage
import streammanagement.GraphBuilderActor.{BuildGraph, BuiltGraph, SinkWithTopic}
import streammanagement.RunningStreamActor.ShutdownGraph

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object RunningStreamActor {

  def props(
             source: Source[ConsumerMessage.CommittableMessage[Array[Byte], String],
               Consumer.Control],
             sink: SinkWithTopic,
             flows: List[Flow[CommitableFilterMessage,CommitableFilterMessage,NotUsed]]
           )
           (implicit materializer: ActorMaterializer): Props = {
    Props(new RunningStreamActor(source, sink, flows))
  }

  case object RunStream

  case object ShutdownGraph

}

class RunningStreamActor(
                          source: Source[ConsumerMessage.CommittableMessage[Array[Byte], String],
                            Consumer.Control],
                          sink: SinkWithTopic,
                          flows: List[Flow[CommitableFilterMessage,CommitableFilterMessage,NotUsed]]
                        )(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  /*
    private implicit val executionContext: ExecutionContext = context.system.dispatcher
  */
  implicit val timeout: Timeout = 2 second

  val graphBuilderActor: ActorRef = context.actorOf(GraphBuilderActor.props())

  val future: Future[BuiltGraph] = ask(graphBuilderActor, BuildGraph(source, sink, flows)).mapTo[BuiltGraph]
  val graph: RunnableGraph[UniqueKillSwitch] = Await.result(future, 2 second).graph

  log.info("running graph")
  var killSwitch: UniqueKillSwitch = graph.run()


  override def preStart(): Unit = {
    log.info("Starting StreamActor ")
  }

  override def postStop(): Unit = {
    log.info("Stopping StreamActor")
  }

  override def receive = {
    case ShutdownGraph =>
      killSwitch.shutdown()
      log.info("Shutdown Graph")
      context.stop(self)

  }
}

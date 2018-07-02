package io.logbee.keyscore.agent.pipeline

import akka.NotUsed
import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import akka.stream._
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Flow, Source, Tcp}
import akka.stream.stage._
import akka.util.ByteString
import io.logbee.keyscore.agent.pipeline.TcpFilterLogic.Message
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.concurrent.Future

object TcpFilterLogic {

  trait Message
  case class HelloMessage() extends Message
  case class PushMessage(payload: Dataset) extends Message
  case class PullMessage() extends Message
}

class TcpFilterLogic (context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  private var connections: Source[IncomingConnection, Future[ServerBinding]] = null

  override def initialize(configuration: FilterConfiguration): Unit = configure(configuration)

  override def configure(configuration: FilterConfiguration): Unit = {

    val host = "localhost"
    val port = 7000

    connections = Tcp().bind(host, port)
    connections.runForeach { connection =>

      val flow = Flow[ByteString]
        .via(new Inbound())
        .via(new Outbound())

      connection.handleWith(flow)
    }
  }

  override def onPush(): Unit = ???

  override def onPull(): Unit = ???

  class Inbound extends GraphStageWithMaterializedValue[FlowShape[ByteString, Message], NotUsed] {

    private val in = Inlet[ByteString]("in")
    private val out = Outlet[Message]("out")

    override def shape: FlowShape[ByteString, Message] = FlowShape(in, out)

    override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, NotUsed) = {
      (new GraphStageLogic(shape) {

        this.setHandler(in, new InHandler {
          override def onPush(): Unit = ???
        })

        this.setHandler(out, new OutHandler {
          override def onPull(): Unit = ???
        })
      }, NotUsed)
    }
  }

}

class Outbound(implicit val system: ActorSystem) extends GraphStageWithMaterializedValue[FlowShape[Message, ByteString], NotUsed] {

  private val in = Inlet[Message]("in")
  private val out = Outlet[ByteString]("out")

  override def shape: FlowShape[Message, ByteString] = FlowShape(in, out)

  private val serialization = SerializationExtension.get(system)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, NotUsed) = {
    (new GraphStageLogic(shape) {

      this.setHandler(in, new InHandler {
        override def onPush(): Unit = {

          val message = grab(in)
          val serializer = serialization.findSerializerFor(message)

          push(out, ByteString(serializer.toBinary(message)))
        }
      })

      this.setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }, NotUsed)
  }
}

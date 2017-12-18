package io.logbee.keyscore.frontier.filter

import akka.NotUsed
import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import filter.{CommitableFilterMessage, Filter}
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.native.Serialization

object ToKafkaProducerFilter {

  def apply(sinkTopic: String) = Flow.fromGraph(new ToKafkaProducerFilter(sinkTopic))
}

class ToKafkaProducerFilter(sinkTopic: String) extends GraphStage[FlowShape[CommitableFilterMessage, ProducerMessage.Message[Array[Byte], String, ConsumerMessage.CommittableOffset]]] {
  val in = Inlet[CommitableFilterMessage]("ToKafka.in")
  val out = Outlet[ProducerMessage.Message[Array[Byte], String, ConsumerMessage.CommittableOffset]]("ToKafka.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val msg = grab(in)

          val msgString = Serialization.write(msg.value)
          val producerMessage = ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
            sinkTopic, msgString
          ), msg.committableOffset)

          push(out, producerMessage)
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
  }
}

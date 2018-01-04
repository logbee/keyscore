package io.logbee.keyscore.frontier.filters

import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.stream._
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

object ToKafkaProducerFilter {

  def apply(sinkTopic: String) = Flow.fromGraph(new ToKafkaProducerFilter(sinkTopic))
}

class ToKafkaProducerFilter(sinkTopic: String) extends GraphStage[FlowShape[CommittableEvent, ProducerMessage.Message[Array[Byte], String, ConsumerMessage.CommittableOffset]]] {

  implicit val formats: DefaultFormats.type = DefaultFormats

  private val in = Inlet[CommittableEvent]("ToKafka.in")
  private val out = Outlet[ProducerMessage.Message[Array[Byte], String, ConsumerMessage.CommittableOffset]]("ToKafka.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val event = grab(in)
          val eventString = Serialization.write(event.payload)
          val producerMessage = ProducerMessage.Message(new ProducerRecord[Array[Byte], String](sinkTopic, eventString), event.offset)

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

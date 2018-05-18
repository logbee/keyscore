package io.logbee.keyscore.frontier.filters

import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.stream._
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import io.logbee.keyscore.model.Field
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, FieldSerializer}

object ToKafkaProducerFilter {

  def apply(sinkTopic: String) = Flow.fromGraph(new ToKafkaProducerFilter(sinkTopic))

}

class ToKafkaProducerFilter(sinkTopic: String) extends GraphStage[FlowShape[CommittableRecord, ProducerMessage.Message[Array[Byte], String, ConsumerMessage.CommittableOffset]]] {

  private val in = Inlet[CommittableRecord]("ToKafka.in")
  private val out = Outlet[ProducerMessage.Message[Array[Byte], String, ConsumerMessage.CommittableOffset]]("ToKafka.out")

  private implicit val formats = DefaultFormats + FieldSerializer[Field[_]]()

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {


          val record = grab(in)

          val recordString = Serialization.write(record.payload)
          val producerMessage = ProducerMessage.Message(new ProducerRecord[Array[Byte], String](sinkTopic, recordString), record.offset)

          println(recordString)

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

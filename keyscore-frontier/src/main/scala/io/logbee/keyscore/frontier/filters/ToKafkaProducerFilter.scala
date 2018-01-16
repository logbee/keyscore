package io.logbee.keyscore.frontier.filters

import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.stream._
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import io.logbee.keyscore.model.Field
import io.logbee.keyscore.model.filter.FilterDescriptor.FilterDescriptor
import io.logbee.keyscore.model.filter.TextParameterDescriptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, FieldSerializer}

object ToKafkaProducerFilter {

  def apply(sinkTopic: String) = Flow.fromGraph(new ToKafkaProducerFilter(sinkTopic))


  val descriptor: FilterDescriptor = {
    FilterDescriptor("StandardKafkaProducer", "KafkaProducer", "Creates a Kafka producer that pushes the data into the given topic.", List(
      TextParameterDescriptor("topic")
    ))
  }
}

class ToKafkaProducerFilter(sinkTopic: String) extends GraphStage[FlowShape[CommittableEvent, ProducerMessage.Message[Array[Byte], String, ConsumerMessage.CommittableOffset]]] {

  private val in = Inlet[CommittableEvent]("ToKafka.in")
  private val out = Outlet[ProducerMessage.Message[Array[Byte], String, ConsumerMessage.CommittableOffset]]("ToKafka.out")

  private implicit val formats = DefaultFormats + FieldSerializer[Field]()

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {


          val event = grab(in)

          val eventString = Serialization.write(event.payload)
          val producerMessage = ProducerMessage.Message(new ProducerRecord[Array[Byte], String](sinkTopic, eventString), event.offset)

          println(eventString)

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

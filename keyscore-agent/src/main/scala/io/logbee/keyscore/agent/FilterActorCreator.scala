import RegexFilterActor.Event
import StreamActor.{StartStream}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.util.matching.Regex

object StreamActor {
  def props(streamId: String, sourceTopic: String, sinkTopic: String, rules: String, nextFilter: ActorRef): Props = Props(new StreamActor(streamId, sourceTopic, sinkTopic, rules, nextFilter))

  case object StartStream
}

/**
  * The Stream Actor connects to the specified source, transforms the streamed data with the given rule and sends the new data to the specified output.
  * @param streamId id for the StreamActor
  * @param sourceTopic KafkaTopic for input
  * @param sinkTopic KafkaTopic for output
  * @param rules 1 Regex rule to apply on the data
  */
class StreamActor(streamId: String, sourceTopic: String, sinkTopic: String, rules: String, nextFilter: ActorRef) extends Actor with ActorLogging {
  val config = ConfigFactory.load()
  implicit val system = context.system
  implicit val streamMaterializer = ActorMaterializer()

  /**
    * Specifies the kafka input server, names this kafka consumer and specifies the consumer config
    * TODO: The consumer should persist the offset for kafka and shouldnt read all data with every new run
    */
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("s415vmmt567.detss.corpintra.net:9092")
    .withGroupId("akka-stream-kafka-test"+streamId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  /**
    * Specifies the kafka output server
    */
  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("s415vmmt567.detss.corpintra.net:9092")

  /**
    * Regex to extract all names of the group tags
    */

  override def postStop(): Unit = log.info("Stopped StreamActor with id {} and rule {}", streamId,rules)

  override def preStart(): Unit = log.info("Started StreamActor with id {} and rules {}", streamId,rules)

  private val groupNamePattern: Regex = "\\(\\?<(\\w*)>".r

  override def receive: Receive = {
    case StartStream =>
      log.info("StreamActor("+streamId+"): get start message")

      val kafkaStream = Consumer.committableSource(consumerSettings, Subscriptions.topics(sourceTopic))

      kafkaStream.map(msg => {
        nextFilter ! Event(msg.record.value())
      }).runWith(Sink.ignore)
  }

}

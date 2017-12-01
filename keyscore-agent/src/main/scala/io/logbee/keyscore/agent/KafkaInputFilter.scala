package io.logbee.keyscore.agent

import RegexFilterActor.Event
import KafkaInputFilter.StartStream
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.model.FilterBlueprint
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.util.matching.Regex

object KafkaInputFilter {
  def props(blueprint: FilterBlueprint, next: ActorRef): Props = {
    val parameters = blueprint.parameters
    Props(new KafkaInputFilter(parameters("streamId"), parameters("kafkaSourceTopic"), parameters("rule"), parameters("server"), parameters("offset"), next))
    throw new IllegalArgumentException
  }

  case object StartStream
}

/**
  * Creates a new consumer for a kafka input
  * @param streamId
  * @param kafkaSourceTopic
  * @param rule
  * @param server
  * @param offset
  * @param nextFilter
  */
class KafkaInputFilter(streamId: String, kafkaSourceTopic: String, rule: String, server: String, offset: String, nextFilter: ActorRef) extends Actor with ActorLogging {
  val config = ConfigFactory.load()
  implicit val system = context.system
  implicit val streamMaterializer = ActorMaterializer()

  /**
    * Specifies the kafka input server, names this kafka consumer and specifies the consumer config
    */
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(server)
    .withGroupId("akka_"+streamId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset)

  override def postStop(): Unit = log.info("Stopped KafkaInput with id {} and rule {}", streamId,rule)

  override def preStart(): Unit = log.info("Started KafkaInput with id {} and rule {}", streamId,rule)

  override def receive: Receive = {
    case StartStream =>
      log.info("KafkaInput("+streamId+"): get start message")

      val kafkaStream = Consumer.committableSource(consumerSettings, Subscriptions.topics(kafkaSourceTopic))

      kafkaStream.map(msg => {
        nextFilter ! Event(msg.record.value())
      }).runWith(Sink.ignore)
  }

}

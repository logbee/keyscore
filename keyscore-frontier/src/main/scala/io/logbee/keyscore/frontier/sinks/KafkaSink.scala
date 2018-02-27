package io.logbee.keyscore.frontier.sinks

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Keep, Sink}
import io.logbee.keyscore.frontier.filters.{CommittableRecord, ToKafkaProducerFilter}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextParameter, TextParameterDescriptor}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}


object KafkaSink {

  def create(filterConfig: FilterConfiguration, actorSystem: ActorSystem): Sink[CommittableRecord, NotUsed] = {
    implicit val system: ActorSystem = actorSystem

    val kafkaConfig = try {
      loadFilterConfiguration(filterConfig)
    } catch {
      case nse: NoSuchElementException => throw nse
    }
    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(kafkaConfig.bootstrapServer)

    val sink = Producer.commitableSink(producerSettings)

    val kafkaSink = ToKafkaProducerFilter(kafkaConfig.sinkTopic).toMat(sink)(Keep.left)

    kafkaSink
  }


  private def loadFilterConfiguration(config: FilterConfiguration): KafkaSinkConfiguration = {
    try {
      val bootstrapServer = config.getParameterValue[String]("bootstrapServer")
      val sinkTopic = config.getParameterValue[String]("sinkTopic")
      KafkaSinkConfiguration(bootstrapServer, sinkTopic)
    } catch {
      case _: NoSuchElementException => throw new NoSuchElementException("Missing parameter in KafkaSink configuration")
    }
  }

  val descriptor: FilterDescriptor = {
    FilterDescriptor("KafkaSink", "Kafka Sink", "Writes the streams output to a given kafka topic", List(
      TextParameterDescriptor("sinkTopic"),
      TextParameterDescriptor("bootstrapServer")
    ), "Sink")
  }

}

case class KafkaSinkConfiguration(bootstrapServer: String, sinkTopic: String)

package io.logbee.keyscore.frontier.sinks

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Keep, Sink}
import io.logbee.keyscore.frontier.filters.{CommittableRecord, ToKafkaProducerFilter}
import io.logbee.keyscore.model.filter.{FilterDescriptor, TextParameterDescriptor}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}


object KafkaSink {

  def create(sinkTopic: String, bootstrapServer: String)(implicit system: ActorSystem): Sink[CommittableRecord, NotUsed] = {
    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)

    val sink = Producer.commitableSink(producerSettings)

    val kafkaSink = ToKafkaProducerFilter(sinkTopic).toMat(sink)(Keep.left)

    kafkaSink
  }

  val descriptor:FilterDescriptor = {
    FilterDescriptor("KafkaSink","Kafka Sink","Writes the streams output to a given kafka topic",List(
      TextParameterDescriptor("sinkTopic"),
      TextParameterDescriptor("bootstrapServer")
    ))
  }
}

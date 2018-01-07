package io.logbee.keyscore.frontier.sinks

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Keep, Sink}
import io.logbee.keyscore.frontier.filters.{CommittableEvent, ToKafkaProducerFilter}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}


object KafkaSink {

  def create(sinkTopic: String, bootstrapServer: String)(implicit system: ActorSystem): Sink[CommittableEvent, NotUsed] = {
    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)

    val sink = Producer.commitableSink(producerSettings)

    val kafkaSink = ToKafkaProducerFilter(sinkTopic).toMat(sink)(Keep.left)

    kafkaSink
  }
}

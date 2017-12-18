package io.logbee.keyscore.frontier.sinks

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Keep, Sink}
import filter.CommitableFilterMessage
import io.logbee.keyscore.frontier.Application.{producerSettings, system}
import io.logbee.keyscore.frontier.filter.ToKafkaProducerFilter
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.Future

object KafkaSink {

  def create(sinkTopic: String, bootstrapServer: String)(implicit system: ActorSystem): Sink[CommitableFilterMessage, NotUsed] = {
    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)

    val sink = Producer.commitableSink(producerSettings)

    val kafkaSink = ToKafkaProducerFilter(sinkTopic).toMat(sink)(Keep.left)

    kafkaSink

  }
}

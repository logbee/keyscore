package io.logbee.keyscore.frontier.sinks

import java.util.Locale

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Keep, Sink}
import io.logbee.keyscore.frontier.filters.{CommittableRecord, ToKafkaProducerFilter}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.sink
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.collection.mutable
import scala.concurrent.Future


object KafkaSink {

  def create(filterConfig: FilterConfiguration, actorSystem: ActorSystem): Sink[CommittableRecord, Future[Done]] = {


    val kafkaConfig = try {
      loadFilterConfiguration(filterConfig)
    } catch {
      case nse: NoSuchElementException => throw nse
    }
    val producerSettings = ProducerSettings(actorSystem, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(kafkaConfig.bootstrapServer)

    val sink = Producer.commitableSink(producerSettings)

    val kafkaSink = ToKafkaProducerFilter(kafkaConfig.sinkTopic).toMat(sink)(Keep.right)

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

  def getDescriptors: mutable.Map[Locale, sink.FilterDescriptor] = {
    val descriptors = mutable.Map.empty[Locale, sink.FilterDescriptor]
    descriptors ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH)
    )
  }

  def descriptor(language: Locale): sink.FilterDescriptor = {
    FilterDescriptor("KafkaSink", "Kafka Sink", "Writes the streams output to a given kafka topic",
      FilterConnection(true), FilterConnection(false), List(
        TextParameterDescriptor("sinkTopic"),
        TextParameterDescriptor("bootstrapServer")
      ), "Sink")
  }

}

case class KafkaSinkConfiguration(bootstrapServer: String, sinkTopic: String)

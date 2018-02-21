package io.logbee.keyscore.frontier.sources

import java.util.UUID

import akka.actor.ActorSystem
import akka.kafka
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream._
import akka.stream.scaladsl.{Keep, Source}
import io.logbee.keyscore.frontier.filters.CommittableRecord
import io.logbee.keyscore.model.TextField
import io.logbee.keyscore.model.filter.{FilterDescriptor, TextParameterDescriptor}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse


object KafkaSource {

  /**
    *
    * @param bootstrapServer Address of the BootstrapServer
    * @param sourceTopic Topic of the KafkaSource you want to read from
    * @param groupID ConsumerID for Kafka to manage offset
    * @param offsetConfig "earliest" to read from the beginning "latest" to read from the end of the topic
    * @param system "Actor System"
    * @return Stoppable Kafka Source
    */
  def create(bootstrapServer: String, sourceTopic: String, groupID: String, offsetConfig: String)(implicit system: ActorSystem): Source[CommittableRecord, UniqueKillSwitch] = {
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats


    val consumerSettings = kafka.ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServer)
      .withGroupId(groupID)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)

    val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(sourceTopic))

    val kafkaSource = source.map { element =>
      val fields = parse(element.record.value())
                    .extract[Map[String, String]]
                    .map(pair => (pair._1, TextField(pair._1, pair._2)))

      CommittableRecord(UUID.randomUUID(), fields, element.committableOffset)
    }.viaMat(KillSwitches.single)(Keep.right)

    kafkaSource
  }

  val descriptor:FilterDescriptor={
    FilterDescriptor("KafkaSource","Kafka Source","Reads from a given kafka topic",List(
      TextParameterDescriptor("bootstrapServer"),
      TextParameterDescriptor("sourceTopic"),
      TextParameterDescriptor("groupID"),
      TextParameterDescriptor("offsetConfig")
    ))
  }
}


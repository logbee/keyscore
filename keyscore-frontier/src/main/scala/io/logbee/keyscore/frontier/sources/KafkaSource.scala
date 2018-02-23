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
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextParameterDescriptor}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse


object KafkaSource {


  def create(config:FilterConfiguration)(implicit system: ActorSystem): Source[CommittableRecord, UniqueKillSwitch] = {
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

    val kafkaSourceConfig = loadFilterConfiguration(config)

    val consumerSettings = kafka.ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaSourceConfig.bootstrapServer)
      .withGroupId(kafkaSourceConfig.groupID)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaSourceConfig.offsetConfig)

    val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(kafkaSourceConfig.sourceTopic))

    val kafkaSource = source.map { element =>
      val fields = parse(element.record.value())
                    .extract[Map[String, String]]
                    .map(pair => (pair._1, TextField(pair._1, pair._2)))

      CommittableRecord(UUID.randomUUID(), fields, element.committableOffset)
    }.viaMat(KillSwitches.single)(Keep.right)

    kafkaSource
  }

  private def loadFilterConfiguration(config:FilterConfiguration): KafkaSourceConfiguration ={
    val bootstrapServer = config.getParameterValue[String]("bootstrapServer")
    val sourceTopic = config.getParameterValue[String]("sourceTopic")
    val groupID = config.getParameterValue[String]("groupID")
    val offsetConfig = config.getParameterValue[String]("offsetConfig")
    new KafkaSourceConfiguration(bootstrapServer,sourceTopic,groupID,offsetConfig)
  }

  val descriptor:FilterDescriptor={
    FilterDescriptor("KafkaSource","Kafka Source","Reads from a given kafka topic",List(
      TextParameterDescriptor("bootstrapServer"),
      TextParameterDescriptor("sourceTopic"),
      TextParameterDescriptor("groupID"),
      TextParameterDescriptor("offsetConfig")
    ),"Source")
  }
}

case class KafkaSourceConfiguration(bootstrapServer:String,sourceTopic:String,groupID:String,offsetConfig:String)

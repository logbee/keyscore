package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.kafka.{ConsumerMessage, ProducerMessage, ProducerSettings}
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Keep, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy, SinkShape, SourceShape}
import io.logbee.keyscore.model.{Dataset, Record}
import io.logbee.keyscore.model.filter.FilterConfiguration
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.{DefaultFormats, Formats, NoTypeHints}
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

import scala.collection.mutable
import scala.concurrent.Promise

class KafkaSinkLogic(configuration: FilterConfiguration, shape: SinkShape[Dataset], actorSystem: ActorSystem) extends SinkLogic(configuration, shape) {

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all
  implicit val system: ActorSystem = actorSystem
  implicit val mat = ActorMaterializer()

  private var queue: SourceQueueWithComplete[ProducerMessage.Message[Array[Byte], String, ConsumerMessage.Committable]] = _

  private var topic: String = _

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    val bootstrapServer: String = configuration.getParameterValue[String]("bootstrapServer")
    topic = configuration.getParameterValue[String]("sinkTopic")

    val settings = producerSettings(bootstrapServer)

    val committableSink = Producer.commitableSink(settings)

    queue = Source.queue(1, OverflowStrategy.backpressure).to(committableSink).run()

  }

  def producerSettings(bootstrapServer: String): ProducerSettings[Array[Byte], String] = {
    val settings = ProducerSettings(actorSystem, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)

    settings
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    dataset.foreach(record => {

      new ProducerRecord[Array[Byte], String](topic, parseRecord(record))
    })
  }

  def parseRecord(record: Record): String = {
    val payload = record.payload

    //    payload.values.foldLeft(Map.empty[String, Any]) {
    //      case (map, (field)) => map + (field.name, field.name)
    //    }

    ""
  }

  case class SinkEntry(dataset: Dataset, promise: Promise[Unit])

}

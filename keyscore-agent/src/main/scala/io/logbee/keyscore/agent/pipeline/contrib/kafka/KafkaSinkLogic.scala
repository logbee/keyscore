package io.logbee.keyscore.agent.pipeline.contrib.kafka

import java.util.{Locale, ResourceBundle, UUID}
import java.util.UUID.fromString

import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, SinkShape}
import io.logbee.keyscore.agent.pipeline.stage.{SinkLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.{Formats, NoTypeHints}

import scala.collection.mutable
import scala.concurrent.Promise
import scala.util.Success

object KafkaSinkLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.kafka.KafkaSinkLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.KafkaSinkLogic"
  private val filterId = "4fedbe8e-115e-4408-ba53-5b627b6e2eaf"

  val descriptorMap = mutable.Map.empty[Locale, FilterDescriptorFragment]


  override def describe: MetaFilterDescriptor = {
    val fragments = Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(UUID.fromString(filterId), filterName, fragments)
  }

  private def descriptor(language: Locale) = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(bundleName, language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = false),
      parameters = List(
            TextParameterDescriptor("bootstrapServer", translatedText.getString("bootstrapServer"), "description"),
            TextParameterDescriptor("topic", translatedText.getString("topic"), "description")
          ), translatedText.getString("category"))
  }

}

class KafkaSinkLogic(context: StageContext, configuration: FilterConfiguration, shape: SinkShape[Dataset]) extends SinkLogic(context, configuration, shape) {

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all
  private var queue: SourceQueueWithComplete[ProducerMessage.Message[Array[Byte], String, Promise[Unit]]] = _

  private val pullAsync = getAsyncCallback[Unit](_ => {
    pull(in)
  })

  private var topic: String = _

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    val bootstrapServer: String = configuration.getParameterValue[String]("bootstrapServer")
    topic = configuration.getParameterValue[String]("topic")

    val settings = producerSettings(bootstrapServer)

    val producer = Producer.flow[Array[Byte], String, Promise[Unit]](settings)

    queue = Source.queue(1, OverflowStrategy.backpressure)
      .via(producer)
      .map(_.message)
      .toMat(Sink.foreach(_.passThrough.success()))(Keep.left)
      .run()
  }

  def producerSettings(bootstrapServer: String): ProducerSettings[Array[Byte], String] = {
    val settings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)

    settings
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    dataset.foreach(record => {

      val promise = Promise[Unit]
      val producerMessage = ProducerMessage.Message(new ProducerRecord[Array[Byte], String](topic, parseRecord(record)), promise)
      queue.offer(producerMessage).flatMap(_ => promise.future).onComplete({
        case Success(()) => pullAsync.invoke()
      })
    })
  }

  def parseRecord(record: Record): String = {
    val payload = record.payload

    val message = payload.values.foldLeft(Map.empty[String, Any]) {
      case (map, field) => map + (field.name -> field.value)
    }

    write(message)
  }

  case class SinkEntry(dataset: Dataset, promise: Promise[Unit])

}

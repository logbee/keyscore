package io.logbee.keyscore.pipeline.contrib.kafka

import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, SinkShape}
import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ExpressionType.RegEx
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic.{bootstrapServerParameter, bootstrapServerPortParameter, topicParameter}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.json4s.Formats
import org.json4s.native.Serialization.write

import scala.concurrent.Promise
import scala.util.Success

object KafkaSinkLogic extends Described {

  val bootstrapServerParameter = TextParameterDescriptor(
    ref = "kafka.sink.bootstrapServer",
    info = ParameterInfo(TextRef("bootstrapServer"), TextRef("bootstrapServerDescription")),
    defaultValue = "example.com",
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
      expressionType = RegEx
    ),
    mandatory = true
  )

  val bootstrapServerPortParameter = NumberParameterDescriptor(
    ref = "kafka.sink.bootstrapServerPort",
    info = ParameterInfo(TextRef("bootstrapServerPort"), TextRef("bootstrapServerPortDescription")),
    defaultValue = 9092,
    range = NumberRange(step = 1, end = 65535),
    mandatory = true
  )

  val topicParameter = TextParameterDescriptor(
    ref = "kafka.sink.topic",
    info = ParameterInfo(TextRef("topic"), TextRef("topicDescription")),
    defaultValue = "topic",
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "4fedbe8e-115e-4408-ba53-5b627b6e2eaf",
    describes = SinkDescriptor(
      name = classOf[KafkaSinkLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SINK, Category("Kafka")),
      parameters = Seq(bootstrapServerParameter, bootstrapServerPortParameter, topicParameter)
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.KafkaSinkLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class KafkaSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape) {

  private implicit val formats: Formats = KeyscoreFormats.formats
  private var queue: SourceQueueWithComplete[ProducerMessage.Message[Array[Byte], String, Promise[Unit]]] = _

  private val pullAsync = getAsyncCallback[Unit](_ => {
    pull(in)
  })

  private var bootstrapServer = bootstrapServerParameter.defaultValue
  private var bootstrapServerPort = bootstrapServerPortParameter.defaultValue
  private var topic = topicParameter.defaultValue

  override def postStop(): Unit = {
    log.info("Kafka sink is stopping.")
    queue.complete()
  }

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {

    bootstrapServer = configuration.getValueOrDefault(bootstrapServerParameter, bootstrapServer)
    bootstrapServerPort = configuration.getValueOrDefault(bootstrapServerPortParameter, bootstrapServerPort)
    topic = configuration.getValueOrDefault(topicParameter, topic)

    val settings = producerSettings(s"$bootstrapServer:$bootstrapServerPort")

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

    dataset.records.foreach(record => {

      val promise = Promise[Unit]
      val producerMessage = ProducerMessage.Message(new ProducerRecord[Array[Byte], String](topic, parseRecord(record)), promise)
      queue.offer(producerMessage).flatMap(_ => promise.future).onComplete({
        case Success(()) => pullAsync.invoke()
      })
    })
  }

  def parseRecord(record: Record): String = {

    val message = record.fields.map(field => (field.name, field.value)).foldLeft(Map.empty[String, Any]) {
      case (map, (name, TextValue(value))) => map + (name -> value)
      case (map, (name, NumberValue(value))) => map + (name -> value)
      case (map, (name, DecimalValue(value))) => map + (name -> value)
      case (map, (name, value: TimestampValue)) => map + (name -> Timestamps.toString(value))
      case (map, (name, _)) => map + (name -> null)
      case (map, _) => map
    }

    write(message)
  }

  case class SinkEntry(dataset: Dataset, promise: Promise[Unit])

}

package io.logbee.keyscore.pipeline.contrib.kafka

import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.Attributes.LogLevels
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{Attributes, OverflowStrategy, QueueOfferResult, SinkShape}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Importance.High
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.metrics.{CounterMetricDescriptor, GaugeMetricDescriptor}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.json4s.Formats

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

object KafkaSinkLogic extends Described {

  val bootstrapServerParameter = TextParameterDescriptor(
    ref = "kafka.sink.bootstrap-server.host",
    info = ParameterInfo(
      displayName = TextRef("bootstrap-server.host.displayName"),
      description = TextRef("bootstrap-server.host.description")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    mandatory = true
  )

  val bootstrapServerPortParameter = NumberParameterDescriptor(
    ref = "kafka.sink.bootstrap-server.port",
    info = ParameterInfo(
      displayName = TextRef("bootstrap-server.port.displayName"),
      description = TextRef("bootstrap-server.port.description")
    ),
    defaultValue = 9092,
    range = NumberRange(step = 1, start = 0, end = 65535),
    mandatory = true
  )

  val topicParameter = TextParameterDescriptor(
    ref = "kafka.sink.topic",
    info = ParameterInfo(
      displayName = TextRef("topic.displayName"),
      description = TextRef("topic.description")
    ),
    defaultValue = "topic",
    mandatory = true
  )

  val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "kafka.sink.fieldName",
    info = ParameterInfo(
      displayName = TextRef("fieldName.displayName"),
      description = TextRef("fieldName.description")
    ),
    defaultValue = "message",
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  val maxMessageSizeParameter = NumberParameterDescriptor(
    ref = "kafka.sink.maxMessageSize",
    info = ParameterInfo(
      displayName = TextRef("maxMessageSize.displayName"),
      description = TextRef("maxMessageSize.description")
    ),
    defaultValue = 1000000,
    mandatory = true,
    range = NumberRange(1, 0, Long.MaxValue)
  )

  override def describe = Descriptor(
    ref = "4fedbe8e-115e-4408-ba53-5b627b6e2eaf",
    describes = SinkDescriptor(
      name = classOf[KafkaSinkLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SINK, Category("Kafka")),
      parameters = Seq(
        bootstrapServerParameter,
        bootstrapServerPortParameter,
        topicParameter,
        fieldNameParameter,
        maxMessageSizeParameter
      ),
      icon = Icon.fromClass(classOf[KafkaSinkLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )

  val datasetsWritten = CounterMetricDescriptor(
    name = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic.datasets-written",
    displayName = TextRef("datasetsWrittenName"),
    description = TextRef("datasetsWrittenDesc"),
    importance = High
  )

  val bytesWritten = GaugeMetricDescriptor(
    name = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic.bytes-written",
    displayName = TextRef("bytesWrittenName"),
    description = TextRef("bytesWrittenDesc"),
    importance = High
  )
}

class KafkaSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape) {

  private implicit val formats: Formats = KeyscoreFormats.formats
  private var queue: SourceQueueWithComplete[ProducerMessage.Envelope[Array[Byte], String, Promise[Unit]]] = _

  private val pullAndUpdateMetricsAsync = getAsyncCallback[Option[Dataset]](dataset => {
    pull(in)
    dataset.foreach(dataset => {
      metrics.collect(KafkaSinkLogic.datasetsWritten).increment()
      metrics.collect(KafkaSinkLogic.bytesWritten).increment(dataset.serializedSize)
    })
  })

  private var bootstrapServer = KafkaSinkLogic.bootstrapServerParameter.defaultValue
  private var bootstrapServerPort = KafkaSinkLogic.bootstrapServerPortParameter.defaultValue
  private var topic = KafkaSinkLogic.topicParameter.defaultValue
  private var fieldName = KafkaSinkLogic.fieldNameParameter.defaultValue
  private var maxMessageSizeBytes = KafkaSinkLogic.maxMessageSizeParameter.defaultValue

  override def postStop(): Unit = {
    log.info("Kafka sink is stopping.")
    queue.complete()
  }

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {

    bootstrapServer = configuration.getValueOrDefault(KafkaSinkLogic.bootstrapServerParameter, bootstrapServer)
    bootstrapServerPort = configuration.getValueOrDefault(KafkaSinkLogic.bootstrapServerPortParameter, bootstrapServerPort)
    topic = configuration.getValueOrDefault(KafkaSinkLogic.topicParameter, topic)
    fieldName = configuration.getValueOrDefault(KafkaSinkLogic.fieldNameParameter, fieldName)
    maxMessageSizeBytes = configuration.getValueOrDefault(KafkaSinkLogic.maxMessageSizeParameter, maxMessageSizeBytes)

    val settings = producerSettings(s"$bootstrapServer:$bootstrapServerPort")

    val producer = Producer.flexiFlow[Array[Byte], String, Promise[Unit]](settings)

    tearDown()

    queue = Source.queue(1, OverflowStrategy.backpressure)
      .via(producer)
      .map(_.passThrough)
      .log(s"KafkaSinkLogic: ${parameters.uuid}").withAttributes(
        Attributes.logLevels(
          onElement = LogLevels.Off,
          onFinish = LogLevels.Info,
          onFailure = LogLevels.Debug
        )
      )
      .toMat(Sink.foreach(_.success(())))(Keep.left)
      .run()

    queue.watchCompletion().onComplete {
      case Success(_) =>
        log.info(s"Internal queue completed. ${parameters.uuid}")
        completeStage()
      case Failure(exception) =>
        log.error(exception, s"Internal queue completed unexpectedly. ${parameters.uuid}")
        failStage(new RuntimeException("Internal queue completed unexpectedly.", exception))
    }
  }

  def producerSettings(bootstrapServer: String): ProducerSettings[Array[Byte], String] = {
    val settings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)
      .withProperty(ProducerConfig.ACKS_CONFIG, "1")
      .withProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "5000000")

    settings
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    val messages = dataset.records.foldLeft(mutable.ListBuffer.empty[(ProducerMessage.Message[Array[Byte], String, Promise[Unit]], Future[Unit])]) { case (result, record) =>
      record.fields.find(field => fieldName == field.name) match {
        case Some(Field(_, textValue @ TextValue(_))) =>
          if (textValue.serializedSize <= maxMessageSizeBytes) {
            val promise = Promise[Unit]
            result += ((ProducerMessage.Message(new ProducerRecord[Array[Byte], String](topic, textValue.value), promise), promise.future))
          }
          else {
            log.error(s"Message too large! Message with size ${textValue.serializedSize} is larger than the configured limit of $maxMessageSizeBytes bytes.")
            log.error(s"Message too large: $textValue")
            result
          }
        case _ =>
          result
      }
    }.toList

    if (messages.nonEmpty) {
      enqueue(dataset, messages.head, messages.tail)
    }
    else {
      pull(in)
    }
  }

  private def enqueue(dataset: Dataset, message: (ProducerMessage.Message[Array[Byte], String, Promise[Unit]], Future[Unit]), tail: List[(ProducerMessage.Message[Array[Byte], String, Promise[Unit]], Future[Unit])]): Unit = {
    val future = for {
      queueReady <- queue.offer(message._1).map({
        case QueueOfferResult.Enqueued =>
          Success
        case QueueOfferResult.Dropped | QueueOfferResult.QueueClosed | QueueOfferResult.Failure(_) =>
          Failure
      })
      _ <- message._2
    } yield (queueReady, tail)

    future.onComplete({
      case Success((_, messages)) if messages.nonEmpty =>
        enqueue(dataset, messages.head, messages.tail)
      case Success((_, _)) =>
        pullAndUpdateMetricsAsync.invoke(dataset)
      case Failure(exception) =>
        log.error(exception, message = "Enqueuing of message failed. Skipping to next Dataset.")
        pullAndUpdateMetricsAsync.invoke(None)
    })
  }

  private def tearDown(): Unit = {
    if (queue != null) {
      queue.complete()
    }
  }
}

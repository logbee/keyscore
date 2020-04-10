package io.logbee.keyscore.pipeline.contrib.kafka

import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.Attributes.LogLevels
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{Attributes, OverflowStrategy, QueueOfferResult, SinkShape}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Importance.High
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.metrics.NumberGaugeMetricDescriptor
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.json4s.Formats

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

object KafkaSinkLogicBase {
  import io.logbee.keyscore.model.util.ToOption.T2OptionT

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

  val dataFieldNameParameter = FieldNameParameterDescriptor(
    ref = "kafka.sink.data.field",
    info = ParameterInfo(
      displayName = TextRef("data.field.displayName"),
      description = TextRef("data.field.description")
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



  val datasetsWritten = NumberGaugeMetricDescriptor(
    name = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic.datasets-written",
    displayName = TextRef("datasetsWrittenName"),
    description = TextRef("datasetsWrittenDesc"),
    importance = High
  )

  val bytesWritten = NumberGaugeMetricDescriptor(
    name = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic.bytes-written",
    displayName = TextRef("bytesWrittenName"),
    description = TextRef("bytesWrittenDesc"),
    importance = High
  )


  val LOCALIZATION = Localization.fromResourceBundle(
    bundleName = classOf[KafkaSinkLogicBase].getName,
    Locale.ENGLISH, Locale.GERMAN
  )
}

abstract class KafkaSinkLogicBase(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape)  {

  private implicit val formats: Formats = KeyscoreFormats.formats
  private var queue: SourceQueueWithComplete[ProducerMessage.Envelope[Array[Byte], String, Promise[Unit]]] = _

  private val pullAndUpdateMetricsAsync = getAsyncCallback[Option[Dataset]](dataset => {
    pull(in)
    dataset.foreach(dataset => {
      metrics.collect(KafkaSinkLogicBase.datasetsWritten).increment()
      metrics.collect(KafkaSinkLogicBase.bytesWritten).increment(dataset.serializedSize)
    })
  })

  private var bootstrapServer = KafkaSinkLogicBase.bootstrapServerParameter.defaultValue
  private var bootstrapServerPort = KafkaSinkLogicBase.bootstrapServerPortParameter.defaultValue
  private var fieldName = KafkaSinkLogicBase.dataFieldNameParameter.defaultValue
  private var maxMessageSizeBytes = KafkaSinkLogicBase.maxMessageSizeParameter.defaultValue

  override def postStop(): Unit = {
    tearDown()
    super.postStop()
  }

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {

    bootstrapServer = configuration.getValueOrDefault(KafkaSinkLogicBase.bootstrapServerParameter, bootstrapServer)
    bootstrapServerPort = configuration.getValueOrDefault(KafkaSinkLogicBase.bootstrapServerPortParameter, bootstrapServerPort)
    fieldName = configuration.getValueOrDefault(KafkaSinkLogicBase.dataFieldNameParameter, fieldName)
    maxMessageSizeBytes = configuration.getValueOrDefault(KafkaSinkLogicBase.maxMessageSizeParameter, maxMessageSizeBytes)

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
          onFailure = LogLevels.Error
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


  protected def addToQueue(dataset: Dataset)(topicFrom: Record => Option[String]): Unit = {

    val messages = dataset.records.foldLeft(mutable.ListBuffer.empty[(ProducerMessage.Message[Array[Byte], String, Promise[Unit]], Future[Unit])]) { case (result, record) =>
      topicFrom(record) match {
        case Some(topic) =>
          record.fields.find(field => fieldName == field.name) match {
            case Some(Field(_, textValue @ TextValue(_, _))) =>
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
        case None =>
          log.debug("Record did not have topic field specified. Dropping the record.") //TODO also remove it from the dataset, so that metrics are correct
          result
      }

    }.toList

    if (messages.nonEmpty) {
      enqueue(dataset, messages)
    }
    else {
      pull(in)
    }
  }

  protected def enqueue(dataset: Dataset, messages: List[(ProducerMessage.Message[Array[Byte], String, Promise[Unit]], Future[Unit])]): Unit = {
    val future = for {
      queueReady <- queue.offer(messages.head._1).map({
        case QueueOfferResult.Enqueued =>
          Success
        case QueueOfferResult.Dropped | QueueOfferResult.QueueClosed | QueueOfferResult.Failure(_) =>
          Failure
      })
      _ <- messages.head._2
    } yield (queueReady, messages.tail)

    future.onComplete({
      case Success((_, remaining_messages)) if remaining_messages.nonEmpty =>
        enqueue(dataset, remaining_messages)
      case Success((_, _)) =>
        pullAndUpdateMetricsAsync.invoke(Some(dataset))
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

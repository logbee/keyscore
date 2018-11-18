package io.logbee.keyscore.pipeline.contrib.kafka

import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, QueueOfferResult, SinkShape}
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
import io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic.{bootstrapServerParameter, bootstrapServerPortParameter, fieldNameParameter, topicParameter}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.json4s.Formats
import org.json4s.native.Serialization.write

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
      expressionType = RegEx
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
    range = NumberRange(step = 1, end = 65535),
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
        fieldNameParameter
      ),
      icon = Icon.fromClass(classOf[KafkaSinkLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic",
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
  private var fieldName = fieldNameParameter.defaultValue

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
    fieldName = configuration.getValueOrDefault(fieldNameParameter, fieldName)

    val settings = producerSettings(s"$bootstrapServer:$bootstrapServerPort")

    val producer = Producer.flow[Array[Byte], String, Promise[Unit]](settings)

    tearDown()

    queue = Source.queue(1, OverflowStrategy.backpressure)
      .via(producer)
      .map(_.message)
      .toMat(Sink.foreach(_.passThrough.success(())))(Keep.left)
      .run()
  }

  def producerSettings(bootstrapServer: String): ProducerSettings[Array[Byte], String] = {
    val settings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)

    settings
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    val messages = dataset.records.foldLeft(mutable.ListBuffer.empty[(ProducerMessage.Message[Array[Byte], String, Promise[Unit]], Future[Unit])]) { case (result, record) =>
      record.fields.find(field => fieldName == field.name) match {
        case Some(Field(_, TextValue(value))) =>
          val promise = Promise[Unit]
          result += ((ProducerMessage.Message(new ProducerRecord[Array[Byte], String](topic, value), promise), promise.future))
        case _ =>
          result
      }
    }.toList

    if (messages.nonEmpty) {
      enqueue(messages.head, messages.tail)
    }
    else {
      pull(in)
    }
  }

  private def enqueue(message: (ProducerMessage.Message[Array[Byte], String, Promise[Unit]], Future[Unit]), tail: List[(ProducerMessage.Message[Array[Byte], String, Promise[Unit]], Future[Unit])]): Unit = {
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
        enqueue(messages.head, messages.tail)
      case Success((_, _)) =>
        pullAsync.invoke(())
      case Failure(exception) =>
        log.error(exception, message = "Enqueuing of message failed. Skipping to next Dataset.")
        pullAsync.invoke(())
    })
  }

  private def tearDown(): Unit = {
    if (queue != null) {
      queue.complete()
    }
  }
}

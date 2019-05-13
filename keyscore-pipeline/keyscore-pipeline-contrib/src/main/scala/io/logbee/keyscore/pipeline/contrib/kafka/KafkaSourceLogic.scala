package io.logbee.keyscore.pipeline.contrib.kafka

import akka.kafka
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscription, Subscriptions}
import akka.stream.Attributes.InputBuffer
import akka.stream.scaladsl.{Sink, SinkQueueWithCancel}
import akka.stream.{Attributes, SourceShape}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Importance.High
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.metrics.{CounterMetricDescriptor, GaugeMetricDescriptor}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.util.{Failure, Success}

object KafkaSourceLogic extends Described {

  val serverParameter = TextParameterDescriptor(
    ref = "kafka.source.server",
    info = ParameterInfo(
      displayName = TextRef("bootstrapServer"),
      description = TextRef("serverDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "example.com",
    mandatory = true
  )

  val portParameter = NumberParameterDescriptor(
    "kafka.source.port",
    ParameterInfo(
      TextRef("port"),
      TextRef("portDescription")),
    defaultValue = 9092L,
    range = NumberRange(step = 1, end = 65535),
    mandatory = true
  )

  val groupIdParameter = TextParameterDescriptor(
    ref = "kafka.source.group",
    info = ParameterInfo(
      displayName = TextRef("groupID"),
      description = TextRef("groupDescription")
    ),
    defaultValue = "groupID",
    mandatory = true
  )

  val offsetParameter = ChoiceParameterDescriptor(
    ref = "kafka.source.offset",
    info = ParameterInfo(
      displayName = TextRef("offsetCommit"),
      description = TextRef("offsetDescription")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = "earliest",
        displayName = TextRef("choiceEarliest"),
        description = TextRef("choiceEarliestDescription")
      ),
      Choice(
        name = "latest",
        displayName = TextRef("choiceLatest"),
        description = TextRef("choiceLatestDescription")
      )
    )
  )

  val topicParameter = TextParameterDescriptor(
    ref = "kafka.source.topic",
    info = ParameterInfo(
      displayName = TextRef("topic"),
      description = TextRef("topicDescription")
    ),
    defaultValue = "topic",
    mandatory = true
  )

  val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "kafka.source.fieldName",
    info = ParameterInfo(
      displayName = TextRef("fieldName.displayName"),
      description = TextRef("fieldName.description")
    ),
    defaultValue = "message",
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "6a9671d9-93a9-4fe4-b779-b4e0cf9a6e6c",
    describes = SourceDescriptor(
      name = classOf[KafkaSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SOURCE, Category("Kafka")),
      parameters = Seq(
        serverParameter,
        portParameter,
        groupIdParameter,
        offsetParameter,
        topicParameter,
        fieldNameParameter
      ),
      icon = Icon.fromClass(classOf[KafkaSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )

  val datasetsRead = CounterMetricDescriptor(
    name = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSourceLogic.datasets-read",
    displayName = TextRef("datasetsReadName"),
    description = TextRef("datasetsReadDesc"),
    importance = High
  )

  val bytesRead = GaugeMetricDescriptor(
    name = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSourceLogic.bytes-read",
    displayName = TextRef("bytesReadName"),
    description = TextRef("bytesReadDesc"),
    importance = High
  )
}

class KafkaSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {
  import KafkaSourceLogic._

  private var queue: SinkQueueWithCancel[Dataset] = _

  private var server = ""
  private var port = portParameter.defaultValue
  private var groupID = ""
  private var offsetConfig = ""
  private var topic = ""
  private var fieldName = fieldNameParameter.defaultValue

  private val pushAsync = getAsyncCallback[Dataset] { dataset =>
    push(out, dataset)
    metrics.collect(datasetsRead).increment()
    metrics.collect(bytesRead).increment(dataset.serializedSize)
  }

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def postStop(): Unit = {
    tearDown()
    log.info("Kafka source is stopping.")
  }

  override def configure(configuration: Configuration): Unit = {

    server = configuration.getValueOrDefault(serverParameter, server)
    port = configuration.getValueOrDefault(portParameter, port)
    groupID = configuration.getValueOrDefault(groupIdParameter, groupID)
    offsetConfig = configuration.getValueOrDefault(offsetParameter, offsetConfig)
    topic = configuration.getValueOrDefault(topicParameter, topic)
    fieldName = configuration.getValueOrDefault(fieldNameParameter, fieldName)

    tearDown()

    queue = createSinkQueue(
      createSettings(
        bootstrapServer = s"$server:$port",
        groupID = groupID,
        offsetConfig = offsetConfig
      ),
      Subscriptions.topics(topic)
    )
  }

  override def onPull(): Unit = {
    queue.pull().onComplete {
      case Success(Some(dataset)) =>
        pushAsync.invoke(dataset)
      case Failure(exception) => log.error(exception, "Failed to pull from kafka consumer queue!")
      case _ => log.info("Failed to pull from kafka consumer queue!")
    }
  }

  private def createSettings(bootstrapServer: String, groupID: String, offsetConfig: String): ConsumerSettings[Array[Byte], String] = {
    kafka.ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServer)
      .withGroupId(groupID)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
  }

  private def createSinkQueue(settings: ConsumerSettings[Array[Byte], String], subscription: Subscription): SinkQueueWithCancel[Dataset] = {

    Consumer.committableSource(settings, subscription).map { message =>

      Dataset(
        MetaData(
          Label("io.logbee.keyscore.pipeline.contrib.kafka.source.MESSAGE_TOPIC", TextValue(message.record.topic())),
          Label("io.logbee.keyscore.pipeline.contrib.kafka.source.MESSAGE_PARTITION", NumberValue(message.record.partition())),
          Label("io.logbee.keyscore.pipeline.contrib.kafka.source.MESSAGE_OFFSET", NumberValue(message.record.offset())),
        ),
        Record(
          Field(fieldName, TextValue(message.record.value()))
        )
      )

    }.runWith(Sink.queue[Dataset].withAttributes(Attributes(InputBuffer(1, 1))))
  }

  private def tearDown(): Unit = {
    if (queue != null) {
      queue.cancel()
    }
  }

}
package io.logbee.keyscore.pipeline.contrib.kafka

import akka.kafka
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Attributes.InputBuffer
import akka.stream.scaladsl.{Sink, SinkQueueWithCancel}
import akka.stream.{Attributes, SourceShape}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ExpressionType.RegEx
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}

import scala.util.Success

object KafkaSourceLogic extends Described {

  val serverParameter = TextParameterDescriptor(
    ref = "kafka.source.server",
    info = ParameterInfo(
      displayName = TextRef("bootstrapServer"),
      description = TextRef("serverDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
      expressionType = RegEx
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
    range = NumberRange(1, 0, 65535),
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

  override def describe = Descriptor(
    ref = "6a9671d9-93a9-4fe4-b779-b4e0cf9a6e6c",
    describes = SourceDescriptor(
      name = classOf[KafkaSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SOURCE, Category("Kafka")),
      parameters = Seq(serverParameter,portParameter,groupIdParameter,offsetParameter,topicParameter),
      icon = Icon.fromClass(classOf[KafkaSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class KafkaSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all

  private var sinkQueue: SinkQueueWithCancel[Dataset] = _

  private var server = ""
  private var port = 9092L
  private var groupID = ""
  private var offsetConfig = ""
  private var topic = ""

  private val pushAsync = getAsyncCallback[Dataset] { dataset =>
    push(shape.out, dataset)
  }

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def postStop(): Unit = {
    log.info("Kafka source is stopping.")
    sinkQueue.cancel()
  }

  override def configure(configuration: Configuration): Unit = {

    server = configuration.getValueOrDefault(KafkaSourceLogic.serverParameter, server)
    port = configuration.getValueOrDefault(KafkaSourceLogic.portParameter, port)
    groupID = configuration.getValueOrDefault(KafkaSourceLogic.groupIdParameter, groupID)
    offsetConfig = configuration.getValueOrDefault(KafkaSourceLogic.offsetParameter, offsetConfig)
    topic = configuration.getValueOrDefault(KafkaSourceLogic.topicParameter, topic)
    val bootstrapServer = s"$server:$port"

    val settings = consumerSettings(bootstrapServer, groupID, offsetConfig)

    val committableSource = Consumer.committableSource(settings, Subscriptions.topics(topic))
    sinkQueue = committableSource.map { message =>
      val fields = parse(message.record.value())
        .extract[Map[String, String]]
        .map(pair => Field(pair._1, TextValue(pair._2)))
      Dataset(MetaData(), Record(fields.toList))
    }.runWith(Sink.queue[Dataset].withAttributes(Attributes(InputBuffer(1, 1))))
  }

  def consumerSettings(bootstrapServer: String, groupID: String, offsetConfig: String): ConsumerSettings[Array[Byte], String] = {
    val settings = kafka.ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServer)
      .withGroupId(groupID)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

    settings
  }

  override def onPull(): Unit = {
    sinkQueue.pull().onComplete {
      case Success(Some(dataset)) => pushAsync.invoke(dataset)
      case _ => log.info("Failed to pull from kafka consumer queue!")
    }
  }
}
package io.logbee.keyscore.agent.pipeline.contrib.kafka

import java.util.{Locale, ResourceBundle, UUID}
import java.util.UUID.fromString

import akka.actor.ActorSystem
import akka.kafka
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, SourceShape}
import io.logbee.keyscore.agent.pipeline.stage.{SourceLogic, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}

import scala.collection.mutable
import scala.concurrent.Promise

object KafkaSourceLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.kafka.KafkaSourceLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.KafkaSourceLogic"
  private val filterId = "6a9671d9-93a9-4fe4-b779-b4e0cf9a6e6c"

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
      previousConnection = FilterConnection(isPermitted = false),
      nextConnection = FilterConnection(isPermitted = true),
      parameters = List(
        TextParameterDescriptor(translatedText.getString("bootstrapServer")),
        TextParameterDescriptor(translatedText.getString("groupID")),
        TextParameterDescriptor(translatedText.getString("offsetCommit")),
        TextParameterDescriptor(translatedText.getString("topic"))
      ), translatedText.getString("category"))
  }
}

class KafkaSourceLogic(context: StageContext, configuration: FilterConfiguration, shape: SourceShape[Dataset]) extends SourceLogic(context, configuration, shape) {

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all
  implicit val system: ActorSystem = context.system
  implicit val mat = ActorMaterializer()

  private val queue = mutable.Queue[SourceEntry]()

  private val insertCallback = getAsyncCallback[SourceEntry](entry => {
    queue.enqueue(entry)
    push()
  })

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    val bootstrapServer: String = configuration.getParameterValue[String]("bootstrapServer")
    val groupID: String = configuration.getParameterValue[String]("groupID")
    val offsetConfig: String = configuration.getParameterValue[String]("offsetCommit")
    val topic: String = configuration.getParameterValue[String]("sourceTopic")

    val settings = consumerSettings(bootstrapServer, groupID, offsetConfig)

    val committableSource = Consumer.committableSource(settings, Subscriptions.topics(topic))

    val kafkaSource = committableSource.map { message =>
      val fields = parse(message.record.value())
        .extract[Map[String, String]]
        .map(pair => (pair._1, TextField(pair._1, pair._2)))

      val dataset = Dataset(Record(fields))
      dataset
    }.mapAsync(1)(dataset => {
      val entry = SourceEntry(dataset, Promise[Unit])
      insertCallback.invoke(entry)
      entry.promise.future
    }).runWith(Sink.ignore)

  }

  def consumerSettings(bootstrapServer: String, groupID: String, offsetConfig: String): ConsumerSettings[Array[Byte], String] = {
    val settings = kafka.ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServer)
      .withGroupId(groupID)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)

    settings
  }

  override def onPull(): Unit = {
    push()
  }

  def push(): Unit = {
    while (queue.nonEmpty) {
      if (isAvailable(shape.out)) {
        val entry = queue.dequeue()
        push(shape.out, entry.dataset)
        entry.promise.success()
      }
    }
  }

  case class SourceEntry(dataset: Dataset, promise: Promise[Unit])


}
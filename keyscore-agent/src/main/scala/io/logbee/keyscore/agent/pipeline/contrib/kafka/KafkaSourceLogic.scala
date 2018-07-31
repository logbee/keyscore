package io.logbee.keyscore.agent.pipeline.contrib.kafka

import java.util.{Locale, ResourceBundle, UUID}

import akka.actor.ActorSystem
import akka.kafka
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Keep, Sink}
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
      previousConnection = FilterConnection(isPermitted = true,connectionType = List(FilterConnectionType.SOURCE)),
      nextConnection = FilterConnection(isPermitted = true),
      parameters = List(
        TextParameterDescriptor("bootstrapServer", translatedText.getString("bootstrapServer"), "description"),
        TextParameterDescriptor("groupID", translatedText.getString("groupID"), "description"),
        TextParameterDescriptor("offsetCommit", translatedText.getString("offsetCommit"), "description"),
        TextParameterDescriptor("topic", translatedText.getString("topic"), "description")
      ), translatedText.getString("category"))
  }
}

class KafkaSourceLogic(context: StageContext, configuration: FilterConfiguration, shape: SourceShape[Dataset]) extends SourceLogic(context, configuration, shape) {

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all
  implicit val system: ActorSystem = context.system
  implicit val mat = ActorMaterializer()

  private var kafkaSource: Consumer.Control = _

  private val queue = mutable.Queue[SourceEntry]()

  private val insertCallback = getAsyncCallback[SourceEntry](entry => {
    queue.enqueue(entry)
    push()
  })

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def postStop(): Unit = {
    log.info("Kafka source is stopping.")
    kafkaSource.stop()
    kafkaSource.shutdown()
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    val bootstrapServer: String = configuration.getParameterValue[String]("bootstrapServer")
    val groupID: String = configuration.getParameterValue[String]("groupID")
    val offsetConfig: String = configuration.getParameterValue[String]("offsetCommit")
    val topic: String = configuration.getParameterValue[String]("topic")

    val settings = consumerSettings(bootstrapServer, groupID, offsetConfig)

    val committableSource = Consumer.committableSource(settings, Subscriptions.topics(topic))

    kafkaSource = committableSource.map { message =>
      val fields = parse(message.record.value())
        .extract[Map[String, String]]
        .map(pair => (pair._1, TextField(pair._1, pair._2)))

      val dataset = Dataset(Record(fields))
      dataset
    }.mapAsync(1)(dataset => {
      val entry = SourceEntry(dataset, Promise[Unit])
      insertCallback.invoke(entry)
      entry.promise.future
    }).toMat(Sink.ignore)(Keep.left).run()

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
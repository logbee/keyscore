package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.kafka
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.{ActorMaterializer, SourceShape}
import akka.stream.scaladsl.Sink
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.{Dataset, Record, TextField}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

class KafkaSourceLogic(configuration: FilterConfiguration, shape: SourceShape[Dataset], actorSystem: ActorSystem) extends SourceLogic(configuration, shape) {

  //TODO Descriptor for KafkaSource

  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  implicit val system: ActorSystem = actorSystem
  implicit val mat = ActorMaterializer()

  private val queue = mutable.Queue[Entry]()

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
      Dataset(Record(fields))
    }.mapAsync(1)(insert).runWith(Sink.ignore)

  }

  def insert(dataset: Dataset): Future[_] = {
    val entry = Entry(dataset, Promise[Unit])
    queue.enqueue(entry)
    entry.promise.future
  }

  def consumerSettings(bootstrapServer: String, groupID: String, offsetConfig: String): ConsumerSettings[Array[Byte], String] = {
    val settings = kafka.ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServer)
      .withGroupId(groupID)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)

    settings
  }

  override def onPull(): Unit = {

    while (isAvailable(shape.out) && queue.nonEmpty) {
      val entry = queue.dequeue()
      push(shape.out, entry.dataset)
      entry.promise.success(())
    }
  }

  case class Entry(dataset: Dataset, promise: Promise[Unit])

}

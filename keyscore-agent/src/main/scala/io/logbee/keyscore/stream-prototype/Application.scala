import akka.actor.ActorSystem
import akka.kafka._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import filter.{AddFieldsFilter, ExtractFieldsFilter}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}
import streammanagement.GraphBuilderActor.SinkWithTopic
import streammanagement.StreamManager
import streammanagement.StreamManager.{ChangeStream, CreateNewStream}

import scala.io.StdIn


object Application extends App {

  implicit val system: ActorSystem = ActorSystem("keyscore-agent")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val sourceTopic = "testfluentd"
  val sinkTopic = "testfluentd"

  /**
    * Specifies the kafka input server, names this kafka consumer and specifies the consumer config
    */
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(""/*BootstrapServer goes here*/)
    .withGroupId("akka-stream-kafka-test-graph-builder")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  /**
    * Specifies the kafka output server
    */
  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(""/*BootstrapServer goes here*/)


  val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(sourceTopic))
  val sink = SinkWithTopic(Producer.commitableSink(producerSettings), sinkTopic)

  val streamManager = system.actorOf(StreamManager.props)


  println("Press Key (2x) to start stream")

  StdIn.readLine()
  StdIn.readLine()

  streamManager ! CreateNewStream(0, source, sink, List(
    ExtractFieldsFilter(List("message", "level", "robotime")),
    ExtractFieldsFilter(List("message", "robotime")),
    AddFieldsFilter(Map("akka_test_field" -> "test value"))
  ))


  println("Press Key to change Stream")
  StdIn.readLine()

  streamManager ! ChangeStream(0, source, sink, List(
    ExtractFieldsFilter(List("logbee_time"))
  ))


  StdIn.readLine("Press Key to Terminate")
  system.terminate()


}

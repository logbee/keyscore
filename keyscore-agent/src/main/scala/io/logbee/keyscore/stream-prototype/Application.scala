package streammanagement

import akka.actor.ActorSystem
import akka.kafka._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import filter.{ExtractFieldsFilter, RegExFilter}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}
import streammanagement.GraphBuilderActor.SinkWithTopic
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
    .withBootstrapServers("s415vmmt567.detss.corpintra.net:9092")
    .withGroupId("akka-stream-kafka-test-graph-builder")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  /**
    * Specifies the kafka output server
    */
  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("s415vmmt567.detss.corpintra.net:9092")


  val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(sourceTopic))
  val sink = SinkWithTopic(Producer.commitableSink(producerSettings),sinkTopic)

  val streamManager = system.actorOf(StreamManager.props)


  println("Press Key (2x) to start stream")

  StdIn.readLine()
  StdIn.readLine()

  streamManager ! CreateNewStream(0, source, sink, List(Flow.fromGraph(new ExtractFieldsFilter(List("message","level"))),
    Flow.fromGraph(new ExtractFieldsFilter(List("message")))))


  println ("Press Key to change Stream")
  StdIn.readLine()

  streamManager ! ChangeStream(0, source, sink, List(Flow.fromGraph(new RegExFilter("(?<level>(?<=\\\"level\\\":\\\")[^\"]*)"))))


  StdIn.readLine("Press Key to Terminate")
  system.terminate()


}

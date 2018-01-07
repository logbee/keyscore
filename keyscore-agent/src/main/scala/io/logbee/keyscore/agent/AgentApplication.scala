package io.logbee.keyscore.agent

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.io.StdIn

case class AgentConfiguration(streamId: String, kafkaSourceTopic: String, rule: String, server: String, offset: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val agentConfigutrationFormat = jsonFormat5(AgentConfiguration)
}

object AgentApplication extends App with JsonSupport {

  if (args.length == 0) {
    println("arg missing: path to config file")
  }

  val jsonConfig = scala.io.Source.fromFile(args(0)).mkString.parseJson
  val config = jsonConfig.convertTo[AgentConfiguration]

  println("Starting with the following config: "+jsonConfig.prettyPrint)

  //val system = ActorSystem()
  //val regexFilterActor = system.actorOf(RegexFilterActor.props(config.rule))
  //val kafkaInput = system.actorOf(KafkaInputFilter.props(config.streamId, config.kafkaSourceTopic, config.rule, config.server, config.offset, regexFilterActor))

  //kafkaInput ! StartStream

  StdIn.readLine()

  //system.terminate()
}
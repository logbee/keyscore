package io.logbee.keyscore.agent.app

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.Agent
import io.logbee.keyscore.agent.Agent.Initialize

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The '''AgentApplication''' is the Main Class in the keyscore-agent package. <br><br>
  * The AgentApplication loads the `Configuration` for the package and creates an [[io.logbee.keyscore.agent.Agent]].
  */
object AgentApplication extends App {

  implicit val timeout: Timeout = 5 seconds

  val config = ConfigFactory.load()
  val system = ActorSystem("keyscore", config.getConfig("production").withFallback(config))
  val agent = system.actorOf(Props[Agent], "agent")

  agent ! Initialize
}

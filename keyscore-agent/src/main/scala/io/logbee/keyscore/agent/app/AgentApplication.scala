package io.logbee.keyscore.agent.app

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import io.logbee.keyscore.agent.Agent
import io.logbee.keyscore.agent.Agent.Initialize

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps


object AgentApplication extends App {

  implicit val timeout: Timeout = 5 seconds

  val system = ActorSystem("keyscore")
  val parent = system.actorOf(Props[Agent], "agent")

  parent ! Initialize

  Await.ready(system.whenTerminated, Duration.Inf)
}

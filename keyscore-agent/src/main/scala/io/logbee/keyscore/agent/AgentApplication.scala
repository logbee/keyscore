package io.logbee.keyscore.agent

import akka.actor.ActorSystem

import scala.io.StdIn

object AgentApplication extends App {

  val system = ActorSystem("keyscore")

  StdIn.readLine()

  system.terminate()
}
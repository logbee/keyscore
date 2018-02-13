package io.logbee.keyscore.agent

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object AgentApplication extends App {

  val system = ActorSystem("keyscore")
  Cluster(system) registerOnMemberUp {
    system.actorOf(Props(classOf[Agent]), "agent")
  }

  Await.ready(system.whenTerminated, Duration.Inf)
}
package io.logbee.keyscore.agent.stream.stage

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor

case class StageContext(system: ActorSystem, dispatcher: ExecutionContextExecutor)

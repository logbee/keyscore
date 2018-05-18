package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.ExecutionContextExecutor

case class StageContext(system: ActorSystem, dispatcher: ExecutionContextExecutor)

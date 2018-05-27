package io.logbee.keyscore.agent.pipeline.stage

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor

case class StageContext(system: ActorSystem, dispatcher: ExecutionContextExecutor)

package io.logbee.keyscore.pipeline.api.stage

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor

case class StageContext(system: ActorSystem, dispatcher: ExecutionContextExecutor)

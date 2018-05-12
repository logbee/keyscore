package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor

trait TestSystemWithMaterializerAndExecutionContext {

  val config: Config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem("keyscore", config.getConfig("test").withFallback(config))
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext
}

package io.logbee.keyscore.agent.pipeline

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor

trait TestSystemWithMaterializerAndExecutionContext {

  val config: Config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem("keyscore", config.getConfig("test").withFallback(config))
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val executionContext: ExecutionContextExecutor = materializer.executionContext
}

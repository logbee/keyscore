package io.logbee.keyscore.pipeline.testkit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps


trait TestActorSystem  {

  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val executionContext: ExecutionContextExecutor = materializer.executionContext
}
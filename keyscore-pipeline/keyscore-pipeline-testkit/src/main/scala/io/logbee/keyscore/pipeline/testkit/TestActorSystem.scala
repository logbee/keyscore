package io.logbee.keyscore.pipeline.testkit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.Await.ready
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps


trait TestActorSystem extends BeforeAndAfterAll  { this: Suite =>

  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val executionContext: ExecutionContextExecutor = materializer.executionContext

  override protected def afterAll(): Unit = {
    ready(system.terminate(), 10 seconds)
  }
}
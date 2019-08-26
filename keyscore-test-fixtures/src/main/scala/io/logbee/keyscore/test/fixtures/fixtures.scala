package io.logbee.keyscore.test

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.language.postfixOps

package object fixtures {

  trait ConfigurableActorSystem {
    implicit val config: Config
    implicit lazy val system: ActorSystem = ActorSystem("keyscore", config)
    implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
    implicit lazy val executionContext: ExecutionContextExecutor = materializer.executionContext
  }

  def withActorSystem(test: ActorSystem => Any)(implicit config: Config = ConfigFactory.load()): Any = {

    val resolvedConfig = if (config.hasPath("test")) config.getConfig("test") else config
    val system = ActorSystem("test-system", resolvedConfig)

    try {
      test(system)
    }
    finally {
      Await.ready(system.terminate(), 10 seconds)
    }
  }

  def withActorUnderTest(props: Props)(test: ActorRef => Any)(implicit system: ActorSystem): Any = {

    test(system.actorOf(props))
  }
}

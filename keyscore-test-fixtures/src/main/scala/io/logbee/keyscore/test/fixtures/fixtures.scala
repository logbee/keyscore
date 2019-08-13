package io.logbee.keyscore.test

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

package object fixtures {

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

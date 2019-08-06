package io.logbee.keyscore.test

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

package object fixtures {

  def withActorSystem(test: ActorSystem => Any): Any = {

    val config = ConfigFactory.load()
    val system = ActorSystem("test-system", config.getConfig("test"))

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

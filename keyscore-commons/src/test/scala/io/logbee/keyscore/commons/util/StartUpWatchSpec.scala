package io.logbee.keyscore.commons.util

import akka.testkit.TestProbe
import com.typesafe.config.{Config, ConfigFactory}
import io.logbee.keyscore.commons.util.StartUpWatch.{Ready, StartUpComplete, StartUpFailed}
import io.logbee.keyscore.model.util.ToFiniteDuration.asFiniteDuration
import io.logbee.keyscore.test.fixtures.ConfigurableActorSystem
import io.logbee.keyscore.test.fixtures.ToActorRef._
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._

class StartUpWatchSpec extends FreeSpec with Matchers with ConfigurableActorSystem {
  override implicit val config: Config = ConfigFactory.parseString(
    """|
       |keyscore {
       |  startup-watch {
       |    retry-interval = 1s
       |    max-retries = 3
       |  }
       |}
       |""".stripMargin).withFallback(ConfigFactory.load().getConfig("test"))
  
  trait Fixture {
    val watchee = TestProbe("watchee")
    val observer = TestProbe("observer")
    
    val testee = system.actorOf(StartUpWatch(watchee))
  }
  
  "A StartUpWatch" - {
    "should notify an observer that the given actors are ready" in new Fixture {
      testee tell(StartUpComplete, observer)
      
      watchee.expectMsg(Ready)
      watchee.expectMsg(Ready)
      testee tell(Ready, watchee)
      
      observer.expectMsg(StartUpComplete)
    }
    
    "should notify an observer when the given actors did not become ready in time" in new Fixture {
      testee tell(StartUpComplete, observer)
      
      for (_ <- 0 until config.getInt("keyscore.startup-watch.max-retries")) {
        watchee.expectMsg(Ready)
      }
      
      observer.expectMsg(StartUpFailed)
    }
    
    "should ignore ready-messages from unknown actors" in new Fixture {
      testee tell(StartUpComplete, observer)

      val unknownActor = TestProbe()
      
      watchee.expectMsg(Ready)
      testee tell(Ready, unknownActor)
      
      val max = config.getDuration("keyscore.startup-watch.retry-interval") * config.getInt("keyscore.startup-watch.max-retries") + 1.seconds
      observer.expectMsg(max, StartUpFailed)
    }
  }
}

package io.logbee.keyscore.commons.replication

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.testkit.TestProbe
import com.typesafe.config.{Config, ConfigFactory}
import io.logbee.keyscore.commons.replication.ReplicationSpec.ExampleCommand
import io.logbee.keyscore.commons.replication.Replicator.Replicate
import io.logbee.keyscore.test.fixtures.ToActorRef._
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ReplicationSpec extends FreeSpec with Matchers {


  "A Replicator" - {

    "should" in new ReplicationFixture {

      testee ! Replicate(ExampleCommand("Hello World"), None, sender)
    }
  }

  trait ReplicationFixture {

    val configuration: Config = ConfigFactory.parseString("")
    implicit val system: ActorSystem = ActorSystem("replication-test-system", configuration)
    val realm = Realm("test")
    val sender = TestProbe("sender")
    val testee: ActorRef[Replicator.Command] = Replication(system).replicator(realm).get
  }
}

object ReplicationSpec {
  case class ExampleCommand(message: String) extends Serializable
}

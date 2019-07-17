package io.logbee.keyscore.commons.serviceregistry

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import com.typesafe.config.{Config, ConfigFactory}
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.Registrar._
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.ServiceKey
import io.logbee.keyscore.test.fixtures.ToActorRef.{TypedProbe2SomeTypedActorRef, TypedProbe2TypedActorRef}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers, Suite}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}


@RunWith(classOf[JUnitRunner])
class ServiceRegistrySpec extends FreeSpec with Matchers with ActorSystems with MockFactory {

  Seq(
    ("local", withLocalActorSystem _),
    ("cluster", withClusteredActorSystem _)
  )
  .foreach { case (kind, actorSystem) =>

    s"In a $kind actor system" - {

      "the registrar of the ServiceRegistry" - {

        "should be of the appropriate type according to the environment" in actorSystem { implicit system =>
          // Is there another way to check that we created the right actor behind the scenes?
          ServiceRegistry(system).registrar.path.name should include (kind)
        }

        "should acknowledge successful commands" in actorSystem { implicit system =>

          val sender = TestProbe[Event]("sender")(system.toTyped)
          val service = TestProbe[AnyRef]("service")(system.toTyped)

          val registrar = ServiceRegistry(system).registrar
          val key = ServiceKey[AnyRef]("test-key")

          registrar ! Register(key, service, sender)

          sender.expectMessage(Registered(key, service, registrar))

          registrar ! Unregister(key, service, sender)

          sender.expectMessage(Unregistered(key, service, registrar))

          registrar ! Subscribe(key, sender)

          sender.expectMessage(Subscribed(key, registrar))

          registrar ! Unsubscribe(key, sender)

          sender.expectMessage(Unsubscribed(key, registrar))
        }

        "should return the list of services associated with a given key" in actorSystem { implicit system =>

          val sender = TestProbe[Event]("sender")(system.toTyped)
          val service1 = TestProbe[AnyRef]("service-1")(system.toTyped)
          val service2 = TestProbe[AnyRef]("service-2")(system.toTyped)

          val registrar = ServiceRegistry(system).registrar
          val key = ServiceKey[AnyRef]("test-key")

          registrar ! Find(key, sender)

          sender.expectMessage(Listing(key, Set.empty[ActorRef[AnyRef]], registrar))

          registrar ! Register(key, service1)

          registrar ! Find(key, sender)

          sender.expectMessage(Listing(key, Set(service1.ref), registrar))

          registrar ! Register(key, service2)

          registrar ! Find(key, sender)

          sender.expectMessage(Listing(key, Set(service1.ref, service2.ref), registrar))
        }
      }
    }
  }

  override protected def localConfig: Config = ConfigFactory.parseString(
    """
      akka.loglevel = "DEBUG"
    """)

  override protected def clusterConfig: Config = ConfigFactory.parseString(
    """
      akka {
        actor {
          loglevel = "DEBUG"
          provider = "cluster"
          debug {
            unhandled = on
          }
        }

        remote {
          netty.tcp {
            hostname = "127.0.0.1"
            port = 2552

            bind-hostname = "127.0.0.1"
            bind-port = 2552
          }
        }
      }
    """)
}

trait ActorSystems extends BeforeAndAfterAll  { this: Suite =>

  private var localSystem: ActorSystem = _
  private var clusterSystem: ActorSystem = _

  protected def localConfig: Config
  protected def clusterConfig: Config

  override protected def beforeAll(): Unit = {
    localSystem = ActorSystem("local-test-system", localConfig)
    clusterSystem = ActorSystem("clustered-test-system", clusterConfig)
  }

  override protected def afterAll(): Unit = {
    Await.ready(localSystem.terminate(), 5 seconds)
    Await.ready(clusterSystem.terminate(), 5 seconds)
  }

  def withLocalActorSystem(test: ActorSystem => Unit): Unit = {

    try {
      test(localSystem)
    }
    finally {
    }
  }

  def withClusteredActorSystem(test: ActorSystem => Unit): Unit = {

    try {
      test(clusterSystem)
    }
    finally {
    }
  }
}

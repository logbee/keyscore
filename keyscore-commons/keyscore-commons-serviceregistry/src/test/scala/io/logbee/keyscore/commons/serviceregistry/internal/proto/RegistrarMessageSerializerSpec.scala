package io.logbee.keyscore.commons.serviceregistry.internal.proto

import java.io.NotSerializableException

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem, ExtendedActorSystem}
import akka.testkit.TestProbe
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.{Registrar, ServiceKey}
import io.logbee.keyscore.model.util.Reflection.Object2ExtendedObject
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class RegistrarMessageSerializerSpec extends FreeSpec with Matchers {

  "A RegistrarMessageSerializer" - {

    implicit val system = ActorSystem()
    val key = ServiceKey[Any]("test")
    val actorRefA = TestProbe("actor-a").ref.toTyped
    val actorRefB = TestProbe("actor-b").ref.toTyped
    val actorRefC = TestProbe("actor-c").ref.toTyped

    val testee = new RegistrarMessageSerializer(system.asInstanceOf[ExtendedActorSystem])

    Seq(
      Registrar.Register(key, actorRefA, actorRefB),
      Registrar.Registered(key, actorRefA, actorRefB),
      Registrar.Unregister(key, actorRefA, actorRefB),
      Registrar.Unregistered(key, actorRefA, actorRefB),
      Registrar.Subscribe(key, actorRefA),
      Registrar.Subscribed(key, actorRefA),
      Registrar.Unsubscribe(key, actorRefA),
      Registrar.Unsubscribed(key, actorRefA),
      Registrar.Find(key, actorRefA),
      Registrar.Listing(key, Set(actorRefA, actorRefB), actorRefC),
    )
    .foreach { message =>
      s"should serialize the ${message.getSimpleClassName} message" in {

        val manifest = testee.manifest(message)
        val binary = testee.toBinary(message)
        val actual = testee.fromBinary(binary, manifest)

        actual shouldBe message
      }
    }

    "should throw an Exception when serializing an unknown message" in {
      an [IllegalArgumentException] should be thrownBy testee.manifest("Unknown Message")
      an [IllegalArgumentException] should be thrownBy testee.toBinary("Unknown Message")
      an [NotSerializableException] should be thrownBy testee.fromBinary(Array[Byte](), "Unknown Message")
    }
  }
}

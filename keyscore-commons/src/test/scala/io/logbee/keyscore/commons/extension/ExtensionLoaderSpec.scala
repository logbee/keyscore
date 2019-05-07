package io.logbee.keyscore.commons.extension

import akka.Done
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.commons.extension.ExtensionLoader.{LoadExtensions, RegisterExtension}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ExtensionLoaderSpec extends TestKit(ActorSystem("spec")) with ImplicitSender with WordSpecLike with Matchers {

  "An ExtensionManager" should {

    val extensionManager = system.actorOf(Props[ExtensionLoader])
    val probe = TestProbe()

    system.eventStream.subscribe(probe.ref, classOf[RegisterExtension])

    "emit RegisterExtension messages for all extensions of passed configurations" in {

      val config = ConfigFactory.parseString("test.extensions: [" +
            "{ type = \"filter\", class = \"io.logbee.keyscore.commons.extension.ExampleFilter\" }," +
            "{ type = \"sink\", class = \"io.logbee.keyscore.commons.extension.ExampleSink\" }" +
          "]")

      extensionManager ! LoadExtensions(config, "test.extensions")

      var message: RegisterExtension = probe.receiveOne(100 millis).asInstanceOf[RegisterExtension]
      message.extensionClass.getName should be (classOf[ExampleFilter].getName)
      message.extensionType should be (FilterExtension)

      message = probe.receiveOne(100 millis).asInstanceOf[RegisterExtension]
      message.extensionClass.getName should be (classOf[ExampleSink].getName)
      message.extensionType should be (SinkExtension)

      expectMsg(Done)
    }

    "not emit a RegisterExtension message if the extension class could not be load" in {
      val config = ConfigFactory.parseString("test.extensions: [" +
        "{ type = \"filter\", class = \"Fubar\" }" +
        "]")

      extensionManager ! LoadExtensions(config, "test.extensions")

      probe.expectNoMessage()
    }

    "not emit a RegisterExtension message for an unknown extension type" in {
      val config = ConfigFactory.parseString("test.extensions: [" +
            "{ type = \"fubar\", class = \"io.logbee.keyscore.commons.extension.ExampleFilter\" }" +
          "]")

      extensionManager ! LoadExtensions(config, "test.extensions")

      probe.expectNoMessage()
    }
  }
}

class ExampleFilter {
}

class ExampleSink {
}
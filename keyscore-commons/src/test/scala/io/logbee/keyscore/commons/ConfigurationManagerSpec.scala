package io.logbee.keyscore.commons

import java.util.UUID

import akka.actor.ActorSystem
import akka.pattern._
import akka.testkit.TestKit
import akka.util.Timeout
import io.logbee.keyscore.commons.ConfigurationManager.{CreateConfigurationRequest, DeleteConfigurationRequest, GetConfigurationRequest, GetConfigurationResponse}
import io.logbee.keyscore.model.configuration.{Configuration, TextParameter}
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidFromString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpecLike, Matchers}

import scala.concurrent.duration._

class ConfigurationManagerSpec extends TestKit(ActorSystem("test")) with FreeSpecLike with Matchers with ScalaFutures {

  implicit val timeout = Timeout(5 seconds)

  "A ConfigurationManager" - {

    val configurationManager = system.actorOf(ConfigurationManager())

    "should manage configurations" in {

      val uuidA: UUID = "a95158dd-b351-4173-a29d-9d61af4be7fa"
      val uuidB: UUID = "efebb94d-965a-4bce-8e0d-642e95314c56"
      val configurationA = Configuration(uuidA, TextParameter("message", "Hello World"))
      val configurationB = Configuration(uuidB, TextParameter("message", "Hello World"))

      configurationManager ! CreateConfigurationRequest(configurationA)
      configurationManager ! CreateConfigurationRequest(configurationB)

      whenReady((configurationManager ? GetConfigurationRequest(uuidA)).mapTo[GetConfigurationResponse]) { response =>

        response.configuration shouldBe 'defined
        response.configuration.get.findTextValue("message") shouldBe Option("Hello World")
      }

      configurationManager ! DeleteConfigurationRequest(uuidA)

      whenReady((configurationManager ? GetConfigurationRequest(uuidA)).mapTo[GetConfigurationResponse]) { response =>

        response.configuration shouldNot be ('defined)
      }
    }
  }
}

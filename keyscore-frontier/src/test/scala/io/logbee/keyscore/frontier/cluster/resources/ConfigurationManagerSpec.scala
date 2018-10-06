package io.logbee.keyscore.frontier.cluster.resources

import akka.testkit.{ImplicitSender, TestKit, TestKitBase}
import akka.util.Timeout
import io.logbee.keyscore.commons.cluster.resources
import io.logbee.keyscore.commons.cluster.resources._
import io.logbee.keyscore.commons.{ConfigurationService, HereIam, WhoIs}
import io.logbee.keyscore.model.configuration.ConfigurationRepository.ROOT_ANCESTOR
import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.descriptor.ParameterRef
import io.logbee.keyscore.test.fixtures.ProductionSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpecLike, Inside, Matchers, OptionValues}

import scala.concurrent.duration._


@RunWith(classOf[JUnitRunner])
class ConfigurationManagerSpec extends ProductionSystemWithMaterializerAndExecutionContext
  with TestKitBase with ImplicitSender with FreeSpecLike with Matchers with ScalaFutures with OptionValues with Inside {

  implicit val timeout = Timeout(5 seconds)

  "A ConfigurationManager" - {

    val configurationManager = system.actorOf(ConfigurationManager())

    "should respond to whoIs messages" in new TestKit(system) with ImplicitSender {

      configurationManager ! WhoIs(ConfigurationService)

      inside(expectMsgType[HereIam]) { case HereIam(service, ref) =>
        ref should not be null
        service shouldBe ConfigurationService
      }
    }

    "(when several revisions of a configuration were committed)" - {

      val id = "c40c5fa4-f179-45f7-8bc5-7ff2fc9b4b6f"

      val exampleConfiguration = Configuration(id, parameters = Seq(
        TextParameter(ParameterRef("hostname"), "example.com")
      ))

      val modifiedConfiguration = exampleConfiguration.update(
        _.parameters :+= NumberParameter(ParameterRef("port"), 2488)
      )

      configurationManager ! CommitConfiguration(exampleConfiguration)
      configurationManager ! CommitConfiguration(modifiedConfiguration)

      val exampleRef = expectMsgType[CommitConfigurationSuccess].ref
      val modifiedRef = expectMsgType[CommitConfigurationSuccess].ref

      "should respond with a ConfigurationRef" in {
        inside(exampleRef) { case ConfigurationRef(uuid, revision, ancestor) =>
          uuid shouldBe id
          revision should not be empty
          ancestor shouldBe ROOT_ANCESTOR
        }
      }

      "should respond the head of the committed Configuration" in {

        configurationManager ! RequestConfigurationHeadRevision(ConfigurationRef(id))

        val configuration = expectMsgType[ConfigurationResponse].configuration.value

        configuration.parameters shouldBe modifiedConfiguration.parameters
      }

      "should respond the Configuration in the specified revision" in {

        configurationManager ! RequestConfigurationRevision(exampleRef)

        val configuration = expectMsgType[ConfigurationResponse].configuration.value

        configuration.parameters shouldBe exampleConfiguration.parameters
      }

      "should respond with a list containing the revisions of the committed Configuration" in {

        configurationManager ! RequestAllConfigurationRevisions(ConfigurationRef(id))

        val response =  expectMsgType[ConfigurationsResponse]

        response.configurations should have size 2
        response.configurations.head.parameters shouldBe modifiedConfiguration.parameters
        response.configurations.last.parameters shouldBe exampleConfiguration.parameters
      }
    }

//    "should manage configurations" in {
//
//      val uuidA = "a95158dd-b351-4173-a29d-9d61af4be7fa"
//      val uuidB = "efebb94d-965a-4bce-8e0d-642e95314c56"
//      val configurationA = Configuration(uuidA, TextParameter("message", "Hello World"))
//      val configurationB = Configuration(uuidB, TextParameter("message", "Hello World"))
//
//      configurationManager ! StoreConfigurationRequest(configurationA)
//      configurationManager ! StoreConfigurationRequest(configurationB)
//
//      whenReady((configurationManager ? GetConfigurationRequest(uuidA)).mapTo[GetConfigurationSuccess]) { response =>
//
//        response.configuration shouldBe 'defined
//        response.configuration.findTextValue("message") shouldBe Option("Hello World")
//      }
//
//      configurationManager ! DeleteConfigurationRequest(uuidA)
//
//      whenReady((configurationManager ? GetConfigurationRequest(uuidA)).mapTo[GetConfigurationSuccess]) { response =>
//
//        response.configuration shouldNot be ('defined)
//      }
//    }
  }
}

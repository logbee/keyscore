package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import io.logbee.keyscore.JsonData
import io.logbee.keyscore.JsonData._
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, MediaType}


@ExtendWith(value = Array(classOf[CitrusExtension]))
class ConfigurationApiSpec extends Matchers {
  implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[ConfigurationApiSpec])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def checkConfiguration(@CitrusResource runner: TestRunner): Unit = {

    val sinkConfiguration = loadJson(JsonData.KafkaSinkConfigurationPath)
    val sinkObject = loadExampleSinkConfiguration

    putSingleConfiguration(runner, sinkObject, sinkConfiguration)
    getSingleConfiguration(runner, sinkObject, 3)
    postSingleConfiguration(runner, sinkConfiguration, sinkObject)

    getAllConfigurations(runner, 1)
    deleteSingleConfig(runner, sinkObject)
    getAllConfigurations(runner, 0)

    putSingleConfiguration(runner, sinkObject, sinkConfiguration)
    deleteAllConfigurations(runner)
    getAllConfigurations(runner, 0)
  }

  def putSingleConfiguration(runner: TestRunner, sinkObject: Configuration, sinkConfig: String): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/configuration/${sinkObject.ref.uuid}")
      .contentType("application/json")
      .payload(sinkConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def getSingleConfiguration(runner: TestRunner, sinkObject: Configuration, expectedNumberOfParams: Int): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/configuration/${sinkObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val configuration = read[Configuration](payload)
        configuration should equal(sinkObject)
        configuration.parameters should have size expectedNumberOfParams
        configuration.ref.uuid shouldBe sinkObject.ref.uuid
        log.info("GetSingleConfiguration successfully: " + configuration)
      })
    )
  }

  def deleteSingleConfig(runner: TestRunner, sinkObject: Configuration): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/configuration/${sinkObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def postSingleConfiguration(runner: TestRunner, sinkConfigString: String, sinkObject: Configuration): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"resources/configuration/${sinkObject.ref.uuid}")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .messageType(MessageType.PLAINTEXT)
      .payload(sinkConfigString)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }


  def getAllConfigurations(runner: TestRunner, expected: Int): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/configuration/*")
    )
    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val configurations = read[Map[ConfigurationRef, Configuration]](payload)
        configurations should have size expected
        if (configurations.nonEmpty) {
          log.info("GetAllConfigurations successfully: " + configurations.head._1.uuid)
        }
      })
    )
  }

  def deleteAllConfigurations(runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/configuration/*"))

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }
}

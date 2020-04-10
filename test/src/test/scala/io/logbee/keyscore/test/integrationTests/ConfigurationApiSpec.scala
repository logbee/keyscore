package io.logbee.keyscore.test.integrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import io.logbee.keyscore.test.util.JsonData._
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.matchers.should.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, MediaType}


@ExtendWith(value = Array(classOf[CitrusExtension]))
class ConfigurationApiSpec extends Matchers {

  private implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[ConfigurationApiSpec])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def checkConfiguration(implicit @CitrusResource runner: TestRunner): Unit = {

    val sinkConfiguration = loadJson(CONFIGURATIONS, K2K, "sinkConfig")
    val sinkObject = loadConfiguration(K2K, "sinkConfig")

    putSingleConfiguration(sinkObject, sinkConfiguration)
    getSingleConfiguration(sinkObject, 3)
    postSingleConfiguration(sinkConfiguration, sinkObject)

    getAllConfigurations(1)
    deleteSingleConfig(sinkObject)
    getAllConfigurations(0)

    putSingleConfiguration(sinkObject, sinkConfiguration)
    deleteAllConfigurations(runner)
    getAllConfigurations(0)
  }

  def putSingleConfiguration(sinkObject: Configuration, sinkConfig: String)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/configuration/${sinkObject.ref.uuid}")
      .contentType("application/json")
      .payload(sinkConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
    )
  }

  def getSingleConfiguration(sinkObject: Configuration, expectedNumberOfParams: Int)(implicit runner: TestRunner): TestAction = {
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
        configuration.parameterSet.parameters should have size expectedNumberOfParams
        configuration.ref.uuid shouldBe sinkObject.ref.uuid
        log.info("GetSingleConfiguration successfully: " + configuration)
      })
    )
  }

  def deleteSingleConfig(sinkObject: Configuration)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/configuration/${sinkObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def postSingleConfiguration(sinkConfigString: String, sinkObject: Configuration)(implicit runner: TestRunner): TestAction = {
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


  def getAllConfigurations(expected: Int)(implicit runner: TestRunner): TestAction = {
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

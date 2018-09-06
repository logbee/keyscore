package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.Using
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus


@ExtendWith(value = Array(classOf[CitrusExtension]))
class ConfiguratonTest extends Matchers {
  implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[ConfiguratonTest])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def checkConfiguration(@CitrusResource runner: TestRunner): Unit = {

    val sourceConfiguration = Using.using(getClass.getResourceAsStream("/JSONFiles/configurations/sinkConfig.json")) { stream =>
      scala.io.Source.fromInputStream(stream).mkString
    }
    val sourceConfig = read[Configuration](sourceConfiguration)

    putSingleConfiguration(runner, sourceConfig, sourceConfiguration)
    getSingleConfiguration(runner, sourceConfig)
//    postSingleConfig (runner, sourceConfig)
    getAllConfigurations(runner, 1)

    deleteSingleConfig(runner, sourceConfig)

    getAllConfigurations(runner, 0)

//    deleteAllConfigurations(runner)
  }

  def putSingleConfiguration(runner: TestRunner, sourceObject: Configuration, sourceConfig: String): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/configuration/${sourceObject.ref.uuid}")
      .contentType("application/json")
      .payload(sourceConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def getSingleConfiguration(runner: TestRunner, sourceObject: Configuration): TestAction = {
    runner.http(action => action.client(frontierClient)
          .send()
          .get(s"resources/configuration/${sourceObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
          .receive()
          .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val configuration = read[Configuration](payload)
        log.info("GetSingleConfiguration successfully: " + configuration.ref.uuid)
      })
    )
  }

  def deleteSingleConfig(runner: TestRunner, sourceObject: Configuration): TestAction = {
    runner.http(action => action.client(frontierClient)
          .send()
          .delete(s"resources/configuration/${sourceObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
          .receive()
          .response(HttpStatus.OK))
  }

  def postSingleConfig(runner: TestRunner, sourceObject: Configuration): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"resources/configuration/${sourceObject.ref.uuid}")
      .contentType("application/json")
      .payload(s"config=$sourceObject")
    )

    runner.http(action => action.client(frontierClient)
          .receive()
          .response(HttpStatus.CREATED))
  }


  def getAllConfigurations(runner: TestRunner, expected: Int): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/configuration")
    )
    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val configurations = read[Map[ConfigurationRef, Configuration]](payload)
        configurations should have size expected
        if(configurations.nonEmpty) {
          log.info("GetAllConfigurations successfully: " + configurations.head._1.uuid)
        }
      })
    )
  }

  def deleteAllConfigurations(runner: TestRunner) : TestAction =  {
    runner.http(action => action.client(frontierClient)
          .send()
          .delete(s"resources/configuration/*"))

    runner.http(action => action.client(frontierClient)
          .receive()
          .response(HttpStatus.OK))
  }
}

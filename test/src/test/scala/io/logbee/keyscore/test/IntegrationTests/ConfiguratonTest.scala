package io.logbee.keyscore.test.IntegrationTests

import java.io.InputStreamReader

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.Using
import org.junit.jupiter.api.Test
import org.json4s.native.Serialization.read
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
    //    val sourceConfigurationReader = new InputStreamReader(getClass.getResourceAsStream("JSONFiles/configurations/sinkConfig.json"))
    val sourceConfig = read[Configuration](sourceConfiguration)

    putConfiguration(runner, sourceConfig, sourceConfiguration)
    getConfiguration(runner)
  }

  def putConfiguration(runner: TestRunner, sourceObject: Configuration, sourceConfig: String) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/configuration/${sourceObject.ref.uuid}")
      .contentType("application/json")
      .payload(sourceConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
  }

  def getConfiguration(runner: TestRunner) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/configuration")
    )

//    runner.http(action => action.client(frontierClient)
//      .receive()
//      .response(HttpStatus.OK)
//      .validationCallback((message, context) => {
//        val payload = message.getPayload.asInstanceOf[String]
//        val configurations = read[Map[ConfigurationRef, Configuration]](payload)
//        configurations should have size 1
//        log.info(configurations.head._1.uuid)
//      })
//    )
  }

}

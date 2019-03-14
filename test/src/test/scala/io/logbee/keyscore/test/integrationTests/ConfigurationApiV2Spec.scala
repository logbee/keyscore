package io.logbee.keyscore.test.integrationTests

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
import io.logbee.keyscore.test.CitrusUtils
import io.logbee.keyscore.test.CitrusUtils.readPayload
import org.json4s.native.Serialization.{read, write}
import org.junit.jupiter.api.{AfterEach, Test}
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, MediaType}


@ExtendWith(value = Array(classOf[CitrusExtension]))
class ConfigurationApiV2Spec extends Matchers {

  private implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[ConfigurationApiV2Spec])

  private val baseUrl = "/resources/configuration"

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @AfterEach
  @CitrusTest
  def tryRemoveAll(@CitrusResource runner: TestRunner): Unit = {
    try {
      runner.http(action => action.client(frontierClient)
        .send()
        .post(s"$baseUrl/_remove")
      )
    } catch {
      case _: Throwable =>
    }
  }

  @Test
  @CitrusTest
  def test_that_a_ConfigurationRef_is_respond_when_a_Configuration_is_committed(@CitrusResource runner: TestRunner): Unit = {

    val sinkConfig = loadConfiguration(K2K, "sinkConfig")

    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"$baseUrl/_commit")
      .contentType("application/json")
      .payload(write(sinkConfig))
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val ref = readPayload[ConfigurationRef](message)
        ref.uuid shouldBe sinkConfig.ref.uuid
        ref.revision should not be empty
        ref.ancestor should not be empty
      })
    )
  }
}

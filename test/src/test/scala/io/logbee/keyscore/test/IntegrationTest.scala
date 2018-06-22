package io.logbee.keyscore.test

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.springframework.http.HttpStatus

import scala.util.parsing.json.JSON


@ExtendWith(value = Array(classOf[CitrusExtension]))
class IntegrationTest extends Matchers {
  private val httpClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def agentResponse(@CitrusResource runner: TestRunner): Unit = {

    runner.http(action => action.client(httpClient)
      .send()
      .get("/agents")
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message,context) => {
        val payload = message.getPayload
        val json =  JSON.parseFull(payload.toString)
        println("test")
      })
      .messageType(MessageType.JSON)
    )


  }
}

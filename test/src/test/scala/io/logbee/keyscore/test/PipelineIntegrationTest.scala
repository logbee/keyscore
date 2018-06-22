
package io.logbee.keyscore.test

import java.io.InputStreamReader

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.commons.json.helper.FilterConfigTypeHints
import io.logbee.keyscore.model.{Health, PipelineConfiguration, PipelineInstance}
import org.json4s.ext.{EnumNameSerializer, JavaTypesSerializers}
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.springframework.http.HttpStatus

import scala.io.Source

@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest extends Matchers {
  private implicit val formats = Serialization.formats(FilterConfigTypeHints).withTypeHintFieldName("parameterType") ++ JavaTypesSerializers.all + new EnumNameSerializer(Health)

  private val httpClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()


  @Test
  @CitrusTest
  def createPipeline(@CitrusResource runner: TestRunner): Unit = {
    val pipeLineConfigString = Source.fromResource("pipelineConfiguration.example.json").mkString
    val pipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipelineConfiguration.example.json"))
    val pipeLineConfig = read[PipelineConfiguration](pipelineReader)

    runner.http(action => action.client(httpClient)
      .send()
      .put("/pipeline/configuration")
      .contentType("application/json")
      .payload(pipeLineConfigString)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.CREATED)
    )

    runner.http(action => action.client(httpClient)
      .send()
      .get("/pipeline/configuration/" + pipeLineConfig.id)
    )

    runner.http(action => action.client(httpClient)
    .receive()
      .response(HttpStatus.OK)
      .validationCallback((message,context) =>{
        val payload = message.getPayload.asInstanceOf[String]
        val config = read[PipelineConfiguration](payload)
        config.name should be equals(pipeLineConfig.name)
      }))

  }
}
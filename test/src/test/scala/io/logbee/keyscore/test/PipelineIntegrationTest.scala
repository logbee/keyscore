
package io.logbee.keyscore.test

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import io.logbee.keyscore.agent.pipeline.ExampleData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.springframework.http.HttpStatus
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest extends Matchers {
  private val httpClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()


  @Test
  @CitrusTest
  def createPipeline(@CitrusResource runner: TestRunner): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints) ++
      org.json4s.ext.JavaTypesSerializers.all
    print(write(ExampleData.pipelineConfiguration))
    runner.http(action => action.client(httpClient)
      .send()
      .put("/pipeline/configuration")
      .contentType("application/json")
      .payload(write(ExampleData.pipelineConfiguration))
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
    )
  }
}
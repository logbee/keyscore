package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.JsonData
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory

@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest2 extends Matchers {

  implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[PipelineIntegrationTest2])

  private val httpClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  private val elasticClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:9200")
    .build()

  @Test
  @CitrusTest
  def createPipeline(@CitrusResource runner: TestRunner): Unit = {
//    val pipelineBlueprint = JsonData.loadExamplePipelineBlueprintJson

  }
}

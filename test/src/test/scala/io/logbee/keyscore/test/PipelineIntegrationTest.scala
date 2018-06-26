
package io.logbee.keyscore.test

import java.io.InputStreamReader

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.commons.json.helper.FilterConfigTypeHints
import io.logbee.keyscore.model.{Green, PipelineConfiguration, PipelineInstance}
import org.json4s.Formats
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.springframework.http.HttpStatus
import io.logbee.keyscore.model.json4s.HealthSerializer
import org.springframework.context.annotation.Description

import scala.io.Source

@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest extends Matchers {
  private implicit val formats: Formats = Serialization.formats(FilterConfigTypeHints).withTypeHintFieldName("parameterType") ++ JavaTypesSerializers.all ++ List(HealthSerializer)
  private val httpClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def createPipeline(@CitrusResource runner: TestRunner): Unit = {
    val kafkaToKaftaPipeLineConfigString = Source.fromResource("pipelineConfiguration.kafkaSourceToKafkaSink.json").mkString
    val KafkaToKafkaPipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipelineConfiguration.kafkaSourceToKafkaSink.json"))
    val kafkaToKafkaPipeLineConfig = read[PipelineConfiguration](KafkaToKafkaPipelineReader)

    val kafkaToElasticPipeLineConfigString = Source.fromResource("pipelineConfiguration.kafkaSourceToElastisSearchSink.json").mkString
    val kafkaToElasticPipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipelineConfiguration.kafkaSourceToElastisSearchSink.json"))
    val kafkaToElasticPipeLineConfig = read[PipelineConfiguration](kafkaToElasticPipelineReader)


    // Create new KafkaToKafka Pipeline

    runner.http(action => action.client(httpClient)
      .send()
      .put("/pipeline/configuration")
      .contentType("application/json")
      .payload(kafkaToKaftaPipeLineConfigString)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.CREATED)
    )

    runner.http(action => action.client(httpClient)
      .send()
      .get("/pipeline/configuration/" + kafkaToKafkaPipeLineConfig.id)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        val config = read[PipelineConfiguration](payload)
        config.filter should have size 1
        config.name should equal(kafkaToKafkaPipeLineConfig.name)
        config.source.id should equal(kafkaToKafkaPipeLineConfig.source.id)
        config.sink.id should equal(kafkaToKafkaPipeLineConfig.sink.id)
        config.filter.head.id should equal(kafkaToKafkaPipeLineConfig.filter.head.id)
      }))

    runner.http(action => action.client(httpClient)
      .send()
      .get("/pipeline/instance/" + kafkaToKafkaPipeLineConfig.id)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        val instance = read[PipelineInstance](payload)
        instance.health should equal(Green)
      })
    )

    // Create new KafkaToElastic Pipeline
//
    runner.http(action => action.client(httpClient)
      .send()
      .put("/pipeline/configuration")
      .contentType("application/json")
      .payload(kafkaToElasticPipeLineConfigString)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
    runner.http(action => action.client(httpClient)
      .send()
      .get("/pipeline/configuration/" + kafkaToElasticPipeLineConfig.id)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        val config = read[PipelineConfiguration](payload)
        config.filter should have size 1
        config.source.id should equal(kafkaToElasticPipeLineConfig.source.id)
      })
    )

    runner.http(action => action.client(httpClient)
        .send()
        .get("/pipeline/instance/" + kafkaToElasticPipeLineConfig.id)
    )

    runner.http(action => action.client(httpClient)
          .receive()
          .response(HttpStatus.OK)
          .validationCallback((message,context) =>  {
            val paylaod = message.getPayload.asInstanceOf[String]
            val instance = read[PipelineInstance](paylaod)
            instance.health should equal(Green)
          })
    )
  }
}


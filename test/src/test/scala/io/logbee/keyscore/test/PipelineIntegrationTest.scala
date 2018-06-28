
package io.logbee.keyscore.test

import java.io.InputStreamReader

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.agent.pipeline.ExampleData
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.json4s.{FieldTypeHints, FilterConfigTypeHints, HealthSerializer}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.springframework.http.HttpStatus

import scala.io.Source

@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest extends Matchers {
  implicit val formats = Serialization.formats(FieldTypeHints + FilterConfigTypeHints) ++ JavaTypesSerializers.all ++ List(HealthSerializer)
  private val httpClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def createPipeline(@CitrusResource runner: TestRunner): Unit = {
    val kafkaToKafkaPipeLineConfigString = Source.fromResource("pipelineConfiguration.kafkaSourceToKafkaSink.json").mkString
    val KafkaToKafkaPipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipelineConfiguration.kafkaSourceToKafkaSink.json"))
    val kafkaToKafkaPipeLineConfig = read[PipelineConfiguration](KafkaToKafkaPipelineReader)

    val kafkaToElasticPipeLineConfigString = Source.fromResource("pipelineConfiguration.kafkaToElasticSearchSink.json").mkString
    val kafkaToElasticPipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipelineConfiguration.kafkaToElasticSearchSink.json"))
    val kafkaToElasticPipeLineConfig = read[PipelineConfiguration](kafkaToElasticPipelineReader)

    val pipelineOneFilter = kafkaToKafkaPipeLineConfig.filter.head
    val pipelineTwoFilter = kafkaToElasticPipeLineConfig.filter.head
    val datasetJsonString = write(List(ExampleData.dataset1))


    println(pipelineOneFilter.id)
    println(pipelineTwoFilter.id)
    println(datasetJsonString)

    // Create new KafkaToKafka Pipeline

    runner.http(action => action.client(httpClient)
      .send()
      .put("/pipeline/configuration")
      .contentType("application/json")
      .payload(kafkaToKafkaPipeLineConfigString)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.CREATED)
    )

    runner.http(action => action.client(httpClient)
      .send()
      .get(s"/pipeline/configuration/${kafkaToKafkaPipeLineConfig.id}")
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

    Thread.sleep(3000)

    runner.http(action => action.client(httpClient)
      .send()
      .get(s"/pipeline/instance/${kafkaToKafkaPipeLineConfig.id}")
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
      .get(s"/pipeline/configuration/${kafkaToElasticPipeLineConfig.id}")
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

    Thread.sleep(3000)

    runner.http(action => action.client(httpClient)
      .send()
      .get(s"/pipeline/instance/${kafkaToElasticPipeLineConfig.id}")
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


    // Insert Data into AddFieldsFilter

//    runner.http(action => action.client(httpClient)
//      .send()
//      .put(s"/filter/${kafkaToKafkaPipeLineConfig.filter.head.id}/insert")
//      .contentType("application/json")
//      .payload(datasetJsonString)
//    )
//
//    runner.http(action => action.client(httpClient)
//        .receive()
//        .response(HttpStatus.ACCEPTED)
//    )

//    runner.http(action => action.client(httpClient)
//          .send()
//          .get(s"/filter/${kafkaToKafkaPipeLineConfig.filter.head.id}/extract?value=1")
//    )
//
//    runner.http(action => action.client(httpClient)
//          .receive()
//          .response(HttpStatus.OK)
//          .validationCallback((message, context) => {
//           val payload = read[List[Dataset]](message.getPayload.asInstanceOf[String])
//            payload should contain only dataset1
//      })
//    )
//
//
//
//    runner.http(action => action.client(httpClient)
//      .send()
//      .post(s"/filter/${kafkaToKafkaPipeLineConfig.filter.head.id}/pause?value=true")
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.ACCEPTED)
//    )

    // Check if no Data is in ElasticSearchSink


    // Delete Pipelines

    runner.http(action => action.client(httpClient)
      .send()
      .delete(s"/pipeline/configuration/${kafkaToElasticPipeLineConfig.id}")
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
    )

    runner.http(action => action.client(httpClient)
      .send()
      .get("/pipeline/instance/*")
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        val instances = read[List[PipelineInstance]](payload)
        instances should have size 1
        instances.head.id shouldBe kafkaToKafkaPipeLineConfig.id
      }))

    runner.http(action => action.client(httpClient)
      .send()
      .delete(s"/pipeline/configuration/${kafkaToElasticPipeLineConfig.id}"))
  }


}


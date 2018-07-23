
package io.logbee.keyscore.test.IntegrationTests

import java.io.InputStreamReader

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.agent.pipeline.ExampleData
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.model.{filter, _}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState, Paused, Running}
import io.logbee.keyscore.model.json4s.{FilterStatusSerializer, HealthSerializer, KeyscoreFormats}
import org.json4s.ShortTypeHints
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import org.json4s.native.JsonMethods._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.springframework.http.HttpStatus

import scala.io.Source
import scala.language.postfixOps

@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest extends Matchers {
  implicit val formats = KeyscoreFormats.formats

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
    val kafkaToKafkaPipeLineConfigString = Source.fromResource("pipelineConfiguration.kafkaSourceToKafkaSink.json").mkString
    val KafkaToKafkaPipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipelineConfiguration.kafkaSourceToKafkaSink.json"))
    val kafkaToKafkaPipeLineConfig = read[PipelineConfiguration](KafkaToKafkaPipelineReader)

    val kafkaToElasticPipeLineConfigString = Source.fromResource("pipelineConfiguration.kafkaToElasticSearchSink.json").mkString
    val kafkaToElasticPipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipelineConfiguration.kafkaToElasticSearchSink.json"))
    val kafkaToElasticPipeLineConfig = read[PipelineConfiguration](kafkaToElasticPipelineReader)

    val kafkaToKafkaWithGrokPipelineConfigString = Source.fromResource("pipelineConfiguration.kafkaToKafkaWithGrokFilter.json").mkString
    val kafkaTokafkaWithGrokPipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipelineConfiguration.kafkaToKafkaWithGrokFilter.json"))
    val kafkaToKafkaWithGrokPipelineConfig = read[PipelineConfiguration](kafkaTokafkaWithGrokPipelineReader)

    val newGrokFilterconfigurationConfigString = Source.fromResource("newGrokFilterConfiguration.json").mkString
    val newGrokFilterconfigurationConfigReader = new InputStreamReader(getClass.getResourceAsStream("/newGrokFilterConfiguration.json"))
    val newGrokFilterconfiguration = read[FilterConfiguration](newGrokFilterconfigurationConfigReader)

    val pipelineOneFilter = kafkaToKafkaPipeLineConfig.filter.head
    val pipelineTwoFilter = kafkaToElasticPipeLineConfig.filter.head
    val pipelineThreeFilter = kafkaToKafkaWithGrokPipelineConfig.filter.head
    val datasets = write(List(dataset1, dataset2, dataset3))
    val newFilterConfiguration = write(newGrokFilterconfiguration)

    println(s"LoggerFilterIdPipelineOne: ${pipelineOneFilter.id}")
    println(s"LoggerFilterIdPipelineTwo: ${pipelineTwoFilter.id}")
    println(s"GrokIdPipelineThree: ${pipelineThreeFilter.id}")
    println(s"datasetList: $datasets")


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

    // Create KafkatoKafka with Grok Pipeline

    runner.http(action => action.client(httpClient)
      .send()
      .put("/pipeline/configuration")
      .contentType("application/json")
      .payload(kafkaToKafkaWithGrokPipelineConfigString)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.CREATED)
    )

    runner.http(action => action.client(httpClient)
      .send()
      .get(s"/pipeline/configuration/${kafkaToKafkaWithGrokPipelineConfig.id}")
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        val config = read[PipelineConfiguration](payload)
        config.filter should have size 1
        config.name should equal(kafkaToKafkaWithGrokPipelineConfig.name)
        config.source.id should equal(kafkaToKafkaWithGrokPipelineConfig.source.id)
        config.sink.id should equal(kafkaToKafkaWithGrokPipelineConfig.sink.id)
        config.filter.head.id should equal(kafkaToKafkaWithGrokPipelineConfig.filter.head.id)
      }))

    Thread.sleep(3000)

    runner.http(action => action.client(httpClient)
      .send()
      .get(s"/pipeline/instance/${kafkaToKafkaWithGrokPipelineConfig.id}")
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

    // Insert and Extract Case

    runner.http(action => action.client(httpClient)
      .send()
      .post(s"/filter/${pipelineOneFilter.id}/pause?value=true"))

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
    runner.http(action => action.client(httpClient)
      .send()
      .get(s"/filter/${pipelineOneFilter.id}/state")
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        val state = read[FilterState](payload)
        state.health shouldBe Green
        state.status shouldBe Paused
      })
    )

    runner.http(action => action.client(httpClient)
      .send()
      .post(s"/filter/${pipelineOneFilter.id}/drain?value=true"))

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )


    runner.http(action => action.client(httpClient)
      .send()
      .put(s"/filter/${pipelineOneFilter.id}/insert")
      .contentType("application/json")
      .payload(datasets)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )


    runner.http(action => action.client(httpClient)
      .send()
      .get(s"/filter/${pipelineOneFilter.id}/extract?value=1")
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = read[List[Dataset]](message.getPayload.asInstanceOf[String])
        payload should have size 1
        payload should contain(dataset3)
      })
    )

    runner.http(action => action.client(httpClient)
      .send()
      .get(s"/filter/${pipelineOneFilter.id}/extract?value=5")
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = read[List[Dataset]](message.getPayload.asInstanceOf[String])
        payload should have size 3
      })
    )
    runner.http(action => action.client(httpClient)
      .send()
      .post(s"/filter/${pipelineOneFilter.id}/pause?value=false")
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )

    runner.http(action => action.client(httpClient)
      .send()
      .post(s"/filter/${pipelineOneFilter.id}/drain?value=false")
    )



    //    Insert TestData and check ElasticSearchSink for proof

    runner.http(action => action.client(httpClient)
      .send()
      .put(s"/filter/${pipelineOneFilter.id}/insert")
      .contentType("application/json")
      .payload(datasets)
    )

    runner.http(action => action.client(httpClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )

    Thread.sleep(6000)

    runner.http(action => action.client(elasticClient)
      .send()
      .get("/test/_search")
    )

    runner.http(action => action.client(elasticClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val response = message.getPayload.asInstanceOf[String]
        val json = parse(response)
        val hits = (json \ "hits" \ "total").extract[Int]
        hits shouldBe 3
      }))


//    // Reconfiguring
//    runner.http(action => action.client(httpClient)
//      .send()
//      .put(s"/filter/${pipelineThreeFilter.id}/config")
//      .contentType("application/json")
//      .payload(newFilterConfiguration)
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.ACCEPTED)
//    )

    //     Delete Pipelines

        runner.http(action => action.client(elasticClient)
          .send()
          .delete("/test"))

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
            instances should have size 2
            instances.head.id shouldBe kafkaToKafkaPipeLineConfig.id
          }))

        runner.http(action => action.client(httpClient)
          .send()
          .delete(s"/pipeline/configuration/*")
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
            instances should have size 0
          })
        )

  }
}


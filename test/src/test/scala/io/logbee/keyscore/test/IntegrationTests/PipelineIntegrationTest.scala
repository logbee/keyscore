
package io.logbee.keyscore.test.IntegrationTests

import java.io.InputStreamReader

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

import scala.io.Source
import scala.language.postfixOps

@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest extends Matchers  {

  implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[PipelineIntegrationTest])

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

    val firstPipelineConfigString = Source.fromResource("firstPipelineConfig.json").mkString
    val firstPipelineReader = new InputStreamReader(getClass.getResourceAsStream("/firstPipelineConfig.json"))
    val firstPipelineConfig = read[PipelineConfiguration](firstPipelineReader)



    // Create new KafkaToKafka Pipeline

//    startPipeline(runner,firstPipelineConfigString,firstPipelineConfig)

    // Insert and Extract Case
//
//    pauseFilter(runner, pipelineOneFilter, "true")
//
//    checkFilteState(runner, pipelineOneFilter, Green, Paused)
//
//    drainFilter(runner, pipelineOneFilter, "true")
//
//    insertDatasetsInFilter(runner, pipelineOneFilter, datasets)
//
//    extractDatasetFromFilter(runner, pipelineOneFilter, 1, 1)
//
//    extractDatasetFromFilter(runner, pipelineOneFilter, 5, 3)
//
//    pauseFilter(runner, pipelineOneFilter, "false")
//
//    drainFilter(runner, pipelineOneFilter, "false")



    //    Insert TestData and check ElasticSearchSink for proof

//    insertDatasetsInFilter(runner, pipelineOneFilter, datasets)
//
//    Thread.sleep(6000)
//
//    checkElasticElements(runner, 3)
//
//    // Reconfiguring
//
//    reconfigureFilter(runner, newFilterConfiguration, pipelineThreeFilter)

    //     Delete Pipelines
//
//    /*removeElasticIndex(runner, "test")
//
//    deletePipeline(runner, kafkaToElasticPipeLineConfig)
//
//    checkRunningInstances(runner, 2)
//
//    deleteAllPipelines(runner)
//
//    checkRunningInstances(runner, 0)*/

  }

//  private def extractDatasetFromFilter(runner: TestRunner, filter: FilterConfiguration, amount: Int, expected: Int) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .get(s"/filter/${filter.id}/extract?value=" + amount)
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.OK)
//      .validationCallback((message, context) => {
//        val payload = read[List[Dataset]](message.getPayload.asInstanceOf[String])
//        payload should have size expected
//      })
//    )
//  }
//
//  private def checkElasticElements(runner: TestRunner, hits: Int) = {
//    runner.http(action => action.client(elasticClient)
//      .send()
//      .get("/test/_search")
//    )
//
//    runner.http(action => action.client(elasticClient)
//      .receive()
//      .response(HttpStatus.OK)
//      .validationCallback((message, context) => {
//        val response = message.getPayload.asInstanceOf[String]
//        val json = parse(response)
//        val hits = (json \ "hits" \ "total").extract[Int]
//        hits shouldBe hits
//      }))
//  }
//
//  private def insertDatasetsInFilter(runner: TestRunner, filter: FilterConfiguration, datasets: String) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .put(s"/filter/${filter.id}/insert")
//      .contentType("application/json")
//      .payload(datasets)
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.ACCEPTED)
//    )
//
//    log.info(s"inserted: $datasets into ${filter.descriptor.displayName}")
//  }
//
//  private def drainFilter(runner: TestRunner, filter: FilterConfiguration, toggle: String) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .post(s"/filter/${filter.id}/drain?value=" + toggle))
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.ACCEPTED)
//    )
//  }
//
//  private def checkFilteState(runner: TestRunner, filter: FilterConfiguration, health: Health, status: FilterStatus) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .get(s"/filter/${filter.id}/state")
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.ACCEPTED)
//      .validationCallback((message, context) => {
//        val payload = message.getPayload.asInstanceOf[String]
//        val state = read[FilterState](payload)
//        state.health shouldBe health
//        state.status shouldBe status
//      })
//    )
//  }
//
//  private def pauseFilter(runner: TestRunner, filter: FilterConfiguration, toggle: String) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .post(s"/filter/${filter.id}/pause?value=" + toggle))
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.ACCEPTED)
//    )
//  }
//
//  private def reconfigureFilter(runner: TestRunner, newConfig: String, filter: FilterConfiguration) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .put(s"/filter/${filter.id}/config")
//      .contentType("application/json")
//      .payload(newConfig)
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.ACCEPTED)
//    )
//    log.info(s"Applied new config to ${filter.descriptor.displayName}: $newConfig")
//
//  }
//
//  private def checkRunningInstances(runner: TestRunner, expectedInstances: Int) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .get("/pipeline/instance/*")
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.OK)
//      .validationCallback((message, context) => {
//        val payload = message.getPayload.asInstanceOf[String]
//        val instances = read[List[PipelineInstance]](payload)
//        instances should have size expectedInstances
//      }))
//  }
//
//  private def deleteAllPipelines(runner: TestRunner) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .delete(s"/pipeline/configuration/*")
//    )
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.OK)
//    )
//  }
//
//  private def deletePipeline(runner: TestRunner, config: PipelineConfiguration) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .delete(s"/pipeline/configuration/${config.id}")
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.OK)
//    )
//  }
//
//  private def removeElasticIndex(runner: TestRunner, index: String) = {
//    runner.http(action => action.client(elasticClient)
//      .send()
//      .delete("/" + index))
//    runner.http(action => action.client(elasticClient)
//      .receive()
//      .response(HttpStatus.OK)
//    )
//  }

//  private def startPipeline(runner: TestRunner, pipelineConfigString: String, pipelineConfiguration: PipelineConfiguration) = {
//    runner.http(action => action.client(httpClient)
//      .send()
//      .put("/pipeline/configuration")
//      .contentType("application/json")
//      .payload(pipelineConfigString)
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.CREATED)
//    )
//
//    runner.http(action => action.client(httpClient)
//      .send()
//      .get(s"/pipeline/configuration/${pipelineConfiguration.id}")
//    )
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.OK)
//      .validationCallback((message, context) => {
//        val payload = message.getPayload.asInstanceOf[String]
//        val config = read[PipelineConfiguration](payload)
//        config.filter should have size 1
//        config.name should equal(pipelineConfiguration.name)
//        config.source.id should equal(pipelineConfiguration.source.id)
//        config.sink.id should equal(pipelineConfiguration.sink.id)
//        config.filter.head.id should equal(pipelineConfiguration.filter.head.id)
//      }))
//
//    Thread.sleep(5000)
//
//    runner.http(action => action.client(httpClient)
//      .send()
//      .get(s"/pipeline/instance/${pipelineConfiguration.id}")
//    )
//
//
//    runner.http(action => action.client(httpClient)
//      .receive()
//      .response(HttpStatus.OK)
//      .validationCallback((message, context) => {
//        val payload = message.getPayload.asInstanceOf[String]
//        val instance = read[PipelineInstance](payload)
//        instance.health should equal(Green)
//      })
//    )
//    log.info(s"Created ${pipelineConfiguration.name} with ${pipelineConfiguration.filter.head.descriptor.displayName}: ${pipelineConfiguration.filter.head.id}")
//  }
}


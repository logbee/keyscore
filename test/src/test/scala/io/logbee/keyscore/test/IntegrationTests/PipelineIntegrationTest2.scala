package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.JsonData._
import io.logbee.keyscore.model.blueprint.ToBase.sealedToBase
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.pipeline.{FilterState, FilterStatus, Paused, Ready}
import io.logbee.keyscore.model.{Green, Health, PipelineInstance}
import org.json4s.native.Serialization.{read, write}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.json4s.native.JsonMethods.parse

@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest2 extends Matchers {

  private implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[PipelineIntegrationTest2])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  private val elasticClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:9200")
    .build()

  private val k2kFilterId = "d2588462-b5f4-4b10-8cbb-7bcceb178cb5"
  private val k2eFilterId = "07fbf227-3cde-4acd-853a-4aa733f5f482"

  @Test
  @CitrusTest
  def createPipeline(implicit @CitrusResource runner: TestRunner): Unit = {
    val k2kObject = loadK2KPipelineBlueprint
    val k2eObject = loadK2EPipelineBlueprint



    creatingKafkaToKafkaPipeline(runner)
    getSinglePipelineBlueprint(k2kObject)

    creatingKafkaToElasticPipeline(runner)
    getSinglePipelineBlueprint(k2eObject)

    Thread.sleep(7000)
    checkHealthStateOfPipelines()

    //    ---insertDatasetsIntok2kPipeline---
//        pauseFilter(k2kFilterId, "true")
//        checkFilterState(k2kFilterId, Green, Paused)
//        drainFilter(k2kFilterId, "true")
//        checkFilterState(k2kFilterId, Green, Ready)
//        insertDatasetsIntoFilter(k2kFilterId, datasets)
//        extractDatsetsFromFilter(1, 1, 1)
//        extractDatsetsFromFilter(1, 5, 3)
//        pauseFilter(k2kFilterId, "false")
//        drainFilter(k2kFilterId, "false")
//
//
//        pauseFilter(k2eFilterId, "true")
//        checkFilterState(k2eFilterId,Green, Paused)
//        drainFilter(k2eFilterId, "true")
//        checkFilterState(k2eFilterId,Green, Ready)
//        insertDatasetsIntoFilter(k2eFilterId, datasets)
//        checkElasticElements(3)


    //    getAllPipelineBlueprints(2)
    //    deleteAllPipelineBlueprints()
    //    getAllPipelineBlueprints(0)
  }

  private def creatingKafkaToKafkaPipeline(implicit runner: TestRunner): TestAction = {
    //    1. sourceBlueprint
    val sourceBlueprint = loadJson(K2KBlueprintsPath, SourceBlueprintPath)
    val sourceObject = loadK2KSourceBlueprint
    putSingleBlueprint(sourceObject, sourceBlueprint)
    //    2. sinkBlueprint
    val sinkBlueprint = loadJson(K2KBlueprintsPath, SinkBlueprintPath)
    val sinkObject = loadK2KSinkBlueprint
    putSingleBlueprint(sinkObject, sinkBlueprint)
    //    3. filterBlueprint
    val filterBlueprint = loadJson(K2KBlueprintsPath, FilterBlueprintPath)
    val filterObject = loadK2KFilterBlueprint
    putSingleBlueprint(filterObject, filterBlueprint)
    //    4. sourceConfiguration
    val sourceConfiguration = loadJson(K2KConfigurationsPath, KafkaSourceConfigurationPath)
    val sourceConfiugrationObject = loadK2KSourceConfiguration
    putSingleConfiguration(sourceConfiugrationObject, sourceConfiguration)
    //    5. sinkConfiguration
    val sinkConfiguration = loadJson(K2KConfigurationsPath, KafkaSinkConfigurationPath)
    val sinkConfigurationObject = loadK2KSinkConfiguration
    putSingleConfiguration(sinkConfigurationObject, sinkConfiguration)
    //    6. filterConfiguration
    val filterConfiguration = loadJson(K2KConfigurationsPath, FilterConfigurationPath)
    val filterConfigurationObject = loadK2KFilterConfiguration
    putSingleConfiguration(filterConfigurationObject, filterConfiguration)
    //    7. pipelineBlueprint
    val pipelineBlueprint = loadJson(K2KBlueprintsPath, PipelineBlueprintPath)
    val pipelineObject = loadK2KPipelineBlueprint
    putSinglePipelineBlueprint(pipelineObject, pipelineBlueprint)
    //    8. startPipeline
    val pipelineRefString = write(pipelineObject.ref)
    startPipeline(pipelineObject, pipelineRefString)
  }

  private def creatingKafkaToElasticPipeline(implicit runner: TestRunner): TestAction = {
    //    1. sourceBlueprint
    val sourceBlueprint = loadJson(K2EBlueprintsPath, SourceBlueprintPath)
    val sourceObject = loadK2ESourceBlueprint
    putSingleBlueprint(sourceObject, sourceBlueprint)
    //    2. sinkBlueprint
    val sinkBlueprint = loadJson(K2EBlueprintsPath, SinkBlueprintPath)
    val sinkObject = loadK2ESinkBlueprint
    putSingleBlueprint(sinkObject, sinkBlueprint)
    //    3. filterBlueprint
    val filterBlueprint = loadJson(K2EBlueprintsPath, FilterBlueprintPath)
    val filterObject = loadK2EFilterBlueprint
    putSingleBlueprint(filterObject, filterBlueprint)
    //    4. sourceConfiguration
    val sourceConfiguration = loadJson(K2EConfigurationsPath, KafkaSourceConfigurationPath)
    val sourceConfiugrationObject = loadK2ESourceConfiguration
    putSingleConfiguration(sourceConfiugrationObject, sourceConfiguration)
    //    5. sinkConfiguration
    val sinkConfiguration = loadJson(K2EConfigurationsPath, KafkaSinkConfigurationPath)
    val sinkConfigurationObject = loadK2ESinkConfiguration
    putSingleConfiguration(sinkConfigurationObject, sinkConfiguration)
    //    6. filterConfiguration
    val filterConfiguration = loadJson(K2EConfigurationsPath, FilterConfigurationPath)
    val filiterConfigurationObject = loadK2EFilterConfiguration
    putSingleConfiguration(filiterConfigurationObject, filterConfiguration)
    //    7. pipelineBlueprint
    val pipelineBlueprint = loadJson(K2EBlueprintsPath, PipelineBlueprintPath)
    val pipelineObject = loadK2EPipelineBlueprint
    putSinglePipelineBlueprint(pipelineObject, pipelineBlueprint)
    //    8. startPipeline
    val pipelineRefString = write(pipelineObject.ref)
    startPipeline(pipelineObject, pipelineRefString)
  }

  def putSinglePipelineBlueprint(pipelineObject: PipelineBlueprint, pipelineConfig: String)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
      .contentType("application/json")
      .payload(pipelineConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def putSingleBlueprint(blueprintObject: SealedBlueprint, pipelineConfig: String)(implicit runner: TestRunner): TestAction = {

    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/blueprint/${blueprintObject.blueprintRef.uuid}")
      .contentType("application/json")
      .payload(pipelineConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def putSingleConfiguration(configurationObject: Configuration, sinkConfig: String)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/configuration/${configurationObject.ref.uuid}")
      .contentType("application/json")
      .payload(sinkConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def getAllPipelineBlueprints(expected: Int)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/blueprint/pipeline/*")
    )
    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val pipelineBlueprints = read[Map[BlueprintRef, PipelineBlueprint]](payload)
        pipelineBlueprints should have size expected
        if (pipelineBlueprints.nonEmpty) {
          log.info("GetAllPipelineBlueprints successfully: " + pipelineBlueprints.head._1.uuid)
        }
      })
    )
  }

  def getSinglePipelineBlueprint(pipelineObject: PipelineBlueprint)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val pipelineBlueprint = read[PipelineBlueprint](payload)
        pipelineBlueprint.ref.uuid should equal(pipelineObject.ref.uuid)
        log.info("GetSinglePipelineBlueprint successfully: " + pipelineBlueprint.ref.uuid)
      })
    )
  }

  def deleteSinglePipelineBlueprint(pipelineObject: PipelineBlueprint)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def deleteAllPipelineBlueprints()(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/blueprint/pipeline/*")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def checkHealthStateOfPipelines()(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"pipeline/instance/*")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        val instances = read[List[PipelineInstance]](payload)
        instances.foreach(instance => {
          instance.health shouldBe Green
        })
      }))
  }

  def startPipeline(pipelineObject: PipelineBlueprint, pipelineRef: String)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/pipeline/configuration/${pipelineObject.ref.uuid}")
      .contentType("application/json")
      .payload(pipelineRef)
    )
  }

  def pauseFilter(filterId: String, toggle: String)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"/filter/${filterId}/pause?value=" + toggle))

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
  }

  def drainFilter(filterId: String, toggle: String)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"/filter/${filterId}/drain?value=" + toggle))

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
  }

  def checkFilterState(filterId: String, health: Health, status: FilterStatus)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"/filter/${filterId}/state")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        val state = read[FilterState](payload)
        state.health shouldBe health
        state.status shouldBe status
      })
    )
  }

  def insertDatasetsIntoFilter(filterId: String, datasets: String)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/filter/${filterId}/insert")
      .contentType("application/json")
      .payload(datasets)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
  }

  def extractDatsetsFromFilter(filterId: String, amount: Int, expect: Int)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"/filter/${filterId}/extract?value=" + amount)
    )
    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = read[List[Dataset]](message.getPayload.asInstanceOf[String])
        payload should have size expect
      })
    )

  }

  def checkElasticElements(expectedHits: Int)(implicit runner: TestRunner): TestAction = {
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
          hits shouldBe hits
        }))
  }

}
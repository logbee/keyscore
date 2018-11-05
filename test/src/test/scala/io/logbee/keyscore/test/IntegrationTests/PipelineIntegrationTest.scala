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
import io.logbee.keyscore.model.data.{Dataset, Health}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.pipeline._
import io.logbee.keyscore.model.PipelineInstance
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.test.fixtures.ExampleData.{datasetMulti1, datasetMulti2}
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.{read, write}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

import scala.concurrent.duration._

@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest extends Matchers {

  private implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[PipelineIntegrationTest])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  private val elasticClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:9200")
    .build()

  //The FilterID must equal the BlueprintRefs of the Filters
  private val k2kFilterId = "24a88215-cfe0-47a1-a889-7f3e9f8260ef"
  private val k2eFilterId = "dc882c27-3de2-4603-b272-b35cf81080e2"

  val k2kObject = loadPipelineBlueprint(K2K, "pipelineBlueprint")
  val k2eObject = loadPipelineBlueprint(K2E, "pipelineBlueprint")

  val datasets = List(datasetMulti1, datasetMulti2)
  val datasetsSerialized = write(datasets)

  var pipelineCount = 0
  var pipelineBlueprintsCount = 0

  @Test
  @CitrusTest
  def integrationTest(implicit @CitrusResource runner: TestRunner): Unit = {

    //Create the first Pipeline: Kafka -> Kafka
    creatingKafkaToKafkaPipeline(runner)
    getSinglePipelineBlueprint(k2kObject)

    //Create the second Pipeline: Kafka -> Elastic
    creatingKafkaToElasticPipeline(runner)
    getSinglePipelineBlueprint(k2eObject)

    //Wait until both Pipelines are materialized
    pollPipelineHealthState(expect = pipelineCount) shouldBe true

    //Test the Valves of the first Pipeline Filter
    pauseFilter(k2kFilterId, "true")
    checkFilterState(k2kFilterId, Green, Paused)
    drainFilter(k2kFilterId, "true")
    checkFilterState(k2kFilterId, Green, Dismantled)
    insertDatasetsIntoFilter(k2kFilterId, datasetsSerialized)

    Thread.sleep(2000)
    extractDatsetsFromFilter(k2kFilterId, 5, 2)

    pauseFilter(k2kFilterId, "false")
    drainFilter(k2kFilterId, "false")
    checkFilterState(k2kFilterId, Green, Running)

    Thread.sleep(2000)
    extractDatsetsFromFilter(k2eFilterId, 10, 0)
    
    //Test the Valves of the second Pipeline Filter
    insertDatasetsIntoFilter(k2kFilterId, datasetsSerialized)

    Thread.sleep(2000)
    extractDatsetsFromFilter(k2eFilterId, 2, 2)
    
    //Wait until all Dataset are pushed to the Elastic index
    pollElasticElements(expect = 2) shouldBe true

    //Cleanup
    cleanUp
  }

  private def creatingKafkaToKafkaPipeline(implicit runner: TestRunner): TestAction = {
    //    1. sourceBlueprint
    val sourceBlueprint = loadJson(BLUEPRINTS, K2K, "sourceBlueprint")
    val sourceObject = loadSourceBlueprint(K2K, "sourceBlueprint")
    putSingleBlueprint(sourceObject, sourceBlueprint)
    //    2. sinkBlueprint
    val sinkBlueprint = loadJson(BLUEPRINTS, K2K, "sinkBlueprint")
    val sinkObject = loadSinkBlueprint(K2K, "sinkBlueprint")
    putSingleBlueprint(sinkObject, sinkBlueprint)
    //    3. filterBlueprint
    val filterBlueprint = loadJson(BLUEPRINTS, K2K, "filterBlueprint")
    val filterObject = loadFilterBlueprint(K2K, "filterBlueprint")
    putSingleBlueprint(filterObject, filterBlueprint)
    //    4. sourceConfiguration
    val sourceConfiguration = loadJson(CONFIGURATIONS, K2K, "sourceConfig")
    val sourceConfiugrationObject = loadConfiguration(K2K, "sourceConfig")
    putSingleConfiguration(sourceConfiugrationObject, sourceConfiguration)
    //    5. sinkConfiguration
    val sinkConfiguration = loadJson(CONFIGURATIONS, K2K, "sinkConfig")
    val sinkConfigurationObject = loadConfiguration(K2K, "sinkConfig")
    putSingleConfiguration(sinkConfigurationObject, sinkConfiguration)
    //    6. filterConfiguration
    val filterConfiguration = loadJson(CONFIGURATIONS, K2K, "filterConfig")
    val filterConfigurationObject = loadConfiguration(K2K, "filterConfig")
    putSingleConfiguration(filterConfigurationObject, filterConfiguration)
    //    7. pipelineBlueprint
    val pipelineBlueprint = loadJson(BLUEPRINTS, K2K, "pipelineBlueprint")
    val pipelineObject = loadPipelineBlueprint(K2K, "pipelineBlueprint")
    putSinglePipelineBlueprint(pipelineObject, pipelineBlueprint)
    //    8. startPipeline
    val pipelineRefString = write(pipelineObject.ref)
    startPipeline(pipelineObject, pipelineRefString)
  }

  private def creatingKafkaToElasticPipeline(implicit runner: TestRunner): TestAction = {
    //    1. sourceBlueprint
    val sourceBlueprint = loadJson(BLUEPRINTS, K2E, "sourceBlueprint")
    val sourceObject = loadSourceBlueprint(K2E, "sourceBlueprint")
    putSingleBlueprint(sourceObject, sourceBlueprint)
    //    2. sinkBlueprint
    val sinkBlueprint = loadJson(BLUEPRINTS, K2E, "sinkBlueprint")
    val sinkObject = loadSinkBlueprint(K2E, "sinkBlueprint")
    putSingleBlueprint(sinkObject, sinkBlueprint)
    //    3. filterBlueprint
    val filterBlueprint = loadJson(BLUEPRINTS, K2E, "filterBlueprint")
    val filterObject = loadFilterBlueprint(K2E, "filterBlueprint")
    putSingleBlueprint(filterObject, filterBlueprint)
    //    4. sourceConfiguration
    val sourceConfiguration = loadJson(CONFIGURATIONS, K2E, "sourceConfig")
    val sourceConfiugrationObject = loadConfiguration(K2E, "sourceConfig")
    putSingleConfiguration(sourceConfiugrationObject, sourceConfiguration)
    //    5. sinkConfiguration
    val sinkConfiguration = loadJson(CONFIGURATIONS, K2E, "sinkConfig")
    val sinkConfigurationObject = loadConfiguration(K2E, "sinkConfig")
    putSingleConfiguration(sinkConfigurationObject, sinkConfiguration)
    //    6. filterConfiguration
    val filterConfiguration = loadJson(CONFIGURATIONS, K2E, "filterConfig")
    val filterConfigurationObject = loadConfiguration(K2E, "filterConfig")
    putSingleConfiguration(filterConfigurationObject, filterConfiguration)
    //    7. pipelineBlueprint
    val pipelineBlueprint = loadJson(BLUEPRINTS, K2E, "pipelineBlueprint")
    val pipelineObject = loadPipelineBlueprint(K2E, "pipelineBlueprint")
    putSinglePipelineBlueprint(pipelineObject, pipelineBlueprint)
    //    8. startPipeline
    val pipelineRefString = write(pipelineObject.ref)
    startPipeline(pipelineObject, pipelineRefString)
  }

  def putSinglePipelineBlueprint(pipelineObject: PipelineBlueprint, pipelineJSON: String)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached PUT PipelineBlueprint for ${pipelineObject.ref.uuid}")
    pipelineBlueprintsCount = pipelineBlueprintsCount + 1

    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
      .contentType("application/json")
      .payload(pipelineJSON)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )

  }

  def putSingleBlueprint(blueprintObject: SealedBlueprint, pipelineJSON: String)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached PUT Blueprint")

    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/blueprint/${blueprintObject.blueprintRef.uuid}")
      .contentType("application/json")
      .payload(pipelineJSON)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def putSingleConfiguration(configurationObject: Configuration, configurationJSON: String)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached PUT Configuraiton for ${configurationObject.ref.uuid}")

    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/configuration/${configurationObject.ref.uuid}")
      .contentType("application/json")
      .payload(configurationJSON)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def startPipeline(pipelineObject: PipelineBlueprint, pipelineID: String)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Start Pipeline for ${pipelineObject.ref.uuid}")
    pipelineCount = pipelineCount + 1

    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/pipeline/blueprint")
      .contentType("application/json")
      .payload(pipelineID)
    )

  }

  def getAllPipelineBlueprints(expected: Int)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached GET All PipelineBlueprints")

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
      })
    )
  }

  def getSinglePipelineBlueprint(pipelineObject: PipelineBlueprint)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached GET PipelineBlueprint for ${pipelineObject.ref.uuid}")

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
      })
    )
  }

  def deleteSinglePipelineBlueprint(pipelineObject: PipelineBlueprint)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached DELETE PipelineBlueprint for ${pipelineObject.ref.uuid}")

    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def pauseFilter(filterId: String, toggle: String)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached Pause Filter for ${filterId}")
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"/filter/${filterId}/pause?value=" + toggle))

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
  }

  def drainFilter(filterId: String, toggle: String)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached Drain Filter for ${filterId}")

    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"/filter/${filterId}/drain?value=" + toggle))

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
  }

  def checkFilterState(filterId: String, health: Health, status: FilterStatus)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached Check Filter State for ${filterId}")

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
    log.debug(s"Reached Insert Dataset for ${filterId} with ${datasets}")

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
    log.debug(s"Reached Extract Datasets for ${filterId}")

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

  private def removeElasticIndex(index: String)(implicit runner: TestRunner): TestAction = {
    log.debug(s"Reached Remove Elastic Index for ${index}")

    runner.http(action => action.client(elasticClient)
      .send()
      .delete("/" + index))

    runner.http(action => action.client(elasticClient)
      .receive()
      .response(HttpStatus.OK)
    )
  }

  //Polling
  def pollPipelineHealthState(maxRetries: Int = 10, interval: FiniteDuration = 2 seconds, expect: Int)(implicit runner: TestRunner): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      log.debug(s"Reached Check Health State for ${expect} Pipelines with $retries retries remaining.")
      var greenInstances: Int = 0

      val instances = checkHealthStateOfPipelines(runner)

      instances.foreach(instance => {
        if (instance.health == Green) greenInstances += 1
      })

      if (greenInstances == expect) return true

      Thread.sleep(interval.toMillis)
      retries -= 1
    }

    false
  }

  private def checkHealthStateOfPipelines(implicit runner: TestRunner): List[PipelineInstance] = {
    var instances: List[PipelineInstance] = List.empty

    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"pipeline/instance/*")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        instances = read[List[PipelineInstance]](payload)
      }))

    instances
  }

  def pollElasticElements(maxRetries: Int = 10, interval: FiniteDuration = 2 seconds, expect: Int)(implicit runner: TestRunner): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      log.debug(s"Reached Check Elastic Elements for ${expect} Elements with $retries retries remaining.")

      val elements = checkElasticElements(runner)

      if (elements == expect) return true

      Thread.sleep(interval.toMillis)
      retries -= 1
    }

    false
  }

  private def checkElasticElements(implicit runner: TestRunner): Int = {
    var hits: Int = -1

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
        hits = (json \ "hits" \ "total").extract[Int]
      }))

    log.debug(s"Hits in elastic: $hits")
    hits
  }

  private def cleanUp(implicit runner: TestRunner): TestAction = {
    removeElasticIndex("test")
    getAllPipelineBlueprints(pipelineBlueprintsCount)
    deleteBlueprints
    getAllPipelineBlueprints(pipelineBlueprintsCount)
    deleteConfigurations
    deletePipelines
  }

  private def deleteConfigurations(implicit runner: TestRunner): TestAction = {
    log.debug(s"Deleting all configurations")

    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"/resources/configuration/*")
    )
  }

  private def deleteBlueprints(implicit runner: TestRunner): TestAction = {
    pipelineBlueprintsCount = 0
    log.debug(s"Deleting all blueprints")

    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"/resources/blueprint/pipeline/*")
    )

    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"/resources/blueprint/*")
    )
  }

  private def deletePipelines(implicit runner: TestRunner): TestAction = {
    log.debug(s"Deleting all pipelines")

    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"/pipeline/blueprint/*")
    )
  }
}
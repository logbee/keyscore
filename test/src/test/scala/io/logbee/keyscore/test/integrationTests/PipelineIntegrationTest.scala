package io.logbee.keyscore.test.integrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import io.logbee.keyscore.model.data.Health
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.metrics.{CounterMetric, NumberGaugeMetric}
import io.logbee.keyscore.model.pipeline._
import io.logbee.keyscore.test.fixtures.ExampleData.{datasetMulti1, datasetMulti2}
import io.logbee.keyscore.test.integrationTests.behaviors._
import io.logbee.keyscore.test.util.JsonData._
import io.logbee.keyscore.test.util.TestData.standardTimestamp
import io.logbee.keyscore.test.util.TestingMethods._
import org.json4s.Formats
import org.json4s.native.Serialization.write
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.{Assertion, Matchers}
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps

/**
  * This Citrus Integration-Test should ensure that all '''basic Filter operations''' work as expected. <br>
  * Also all inserted data should be passed through to the end of the pipeline as expected. <br><br>
  *
  * Therefor two pipelines with JSON De-/Encoder and one Filters each are created. <br>
  * Various Filter operations(''insert, extract, status'') are tested with multiple Datasets.
  */
@ExtendWith(value = Array(classOf[CitrusExtension]))
class PipelineIntegrationTest extends Matchers {

  private implicit val formats: Formats = KeyscoreFormats.formats

  private implicit val logger: Logger = LoggerFactory.getLogger(classOf[PipelineIntegrationTest])

  private implicit val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  private val elasticClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:9200")
    .build()

  //The FilterID must equal the BlueprintRefs of the Filters
  //From the belonging JSONs
  private val k2kSinkId = "80851e06-7191-4d96-8e4d-de66a5a12e81"
  private val k2kFilterId = "24a88215-cfe0-47a1-a889-7f3e9f8260ef"
  private val k2eFilterId = "dc882c27-3de2-4603-b272-b35cf81080e2"
  private val k2eSourceId = "4a696573-ce73-4bb2-8d9a-b2a90e834153"

  val k2kObject: PipelineBlueprint = loadPipelineBlueprint(K2K, "pipelineBlueprint")
  val k2eObject: PipelineBlueprint = loadPipelineBlueprint(K2E, "pipelineBlueprint")

  val datasets = List(datasetMulti1, datasetMulti2)
  val datasetsSerialized: String = write(datasets)

  var pipelineCount = 0
  var pipelineBlueprintsCount = 0

  /*
  * TODO:fromMillis
  * 3. Check if all Fields of all Records of all Datasets are passing through the pipeline
  * 4. Check if the datasets in elastic are the same as the original datasets
  */

  @Test
  @CitrusTest
  def runPipelineIntegrationTest(implicit @CitrusResource runner: TestRunner): Unit = {
    import runner.applyBehavior

    //Create the first Pipeline: Kafka -> Kafka
    creatingKafkaToKafkaPipeline(runner)
    checkSinglePipelineBlueprint(k2kObject)

    //Create the second Pipeline: Kafka -> Elastic
    creatingKafkaToElasticPipeline(runner)
    checkSinglePipelineBlueprint(k2eObject)

    //Wait until both Pipelines are materialized
    pollPipelineHealthState(expect = pipelineCount) shouldBe true

    //Test the Valves of the first Pipeline Filter
    applyBehavior(new FilterPause(k2kFilterId, "true"))
    checkFilterState(k2kFilterId, Green, Paused)
    applyBehavior(new FilterDrain(k2kFilterId, "true"))
    checkFilterState(k2kFilterId, Green, Dismantled)
    applyBehavior(new InsertDatasets(k2kFilterId, datasetsSerialized))

    Thread.sleep(2000)
    checkExtractedDatasets(k2kFilterId, 5, 2)

    applyBehavior(new FilterPause(k2kFilterId, "false"))
    applyBehavior(new FilterDrain(k2kFilterId, "false"))
    checkFilterState(k2kFilterId, Green, Running)

    Thread.sleep(2000)
    checkExtractedDatasets(k2eFilterId, 10, 0)

    //Test the Valves of the second Pipeline Filter
    applyBehavior(new InsertDatasets(k2kFilterId, datasetsSerialized))

    Thread.sleep(2000)
    checkExtractedDatasets(k2eFilterId, 2, 2)

    //Wait until all Dataset are pushed to the Elastic index
    pollElasticElements(topic = "test", expect = 2)(runner, elasticClient, logger) shouldBe true

    scrapeMetrics(k2eSourceId, write(standardTimestamp)).last.find[NumberGaugeMetric]("io.logbee.keyscore.pipeline.contrib.kafka.KafkaSourceLogic.datasets-read").get.value shouldBe 2L
    scrapeMetrics(k2eSourceId, write(standardTimestamp)).last.find[NumberGaugeMetric]("io.logbee.keyscore.pipeline.contrib.kafka.KafkaSourceLogic.bytes-read").get.value should be > 595L
    scrapeMetrics(k2kSinkId, write(standardTimestamp)).last.find[NumberGaugeMetric]("io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic.datasets-written").get.value shouldBe 2L
    scrapeMetrics(k2kSinkId, write(standardTimestamp)).last.find[NumberGaugeMetric]("io.logbee.keyscore.pipeline.contrib.kafka.KafkaSinkLogic.bytes-written").get.value should be > 800L

    //Cleanup
    cleanIntegrationTest
  }

  private def creatingKafkaToKafkaPipeline(implicit runner: TestRunner): TestAction = {
    import runner.applyBehavior

    // 1.1.1 Kafka-Source Blueprint
    val sourceBlueprint = loadJson(BLUEPRINTS, K2K, "sourceBlueprint")
    val sourceObject = loadSourceBlueprint(K2K, "sourceBlueprint")
    applyBehavior(new PutSingleBlueprint(sourceObject, sourceBlueprint))

    // 1.1.2 JsonDecoder-Filter Blueprint
    val jsonDecoderBlueprint = loadJson(BLUEPRINTS, K2K, "jsonDecoderBlueprint")
    val jsonDecoderBlueprintObject = loadFilterBlueprint(K2K, "jsonDecoderBlueprint")
    applyBehavior(new PutSingleBlueprint(jsonDecoderBlueprintObject, jsonDecoderBlueprint))

    // 1.1.3 RemoveFields-Filter Blueprint
    val removeFieldsBlueprint = loadJson(BLUEPRINTS, K2K, "removeFieldsBlueprint")
    val removeFieldsObject = loadFilterBlueprint(K2K, "removeFieldsBlueprint")
    applyBehavior(new PutSingleBlueprint(removeFieldsObject, removeFieldsBlueprint))

    // 1.1.4 JsonEncoder-Filter Blueprint
    val jsonEncoderBlueprint = loadJson(BLUEPRINTS, K2K, "jsonEncoderBlueprint")
    val jsonEncoderBlueprintObject = loadFilterBlueprint( K2K, "jsonEncoderBlueprint")
    applyBehavior(new PutSingleBlueprint(jsonEncoderBlueprintObject, jsonEncoderBlueprint))

    // 1.1.5 Kafka-Sink Blueprint
    val sinkBlueprint = loadJson(BLUEPRINTS, K2K, "sinkBlueprint")
    val sinkObject = loadSinkBlueprint(K2K, "sinkBlueprint")
    applyBehavior(new PutSingleBlueprint(sinkObject, sinkBlueprint))

    // 1.1.6 Pipeline Blueprint
    val pipelineBlueprint = loadJson(BLUEPRINTS, K2K, "pipelineBlueprint")
    val pipelineObject = loadPipelineBlueprint(K2K, "pipelineBlueprint")
    putSinglePipelineBlueprint(pipelineObject, pipelineBlueprint)

    // 1.2.1 Kafka-Source Configuration
    val sourceConfiguration = loadJson(CONFIGURATIONS, K2K, "sourceConfig")
    val sourceConfigurationObject = loadConfiguration(K2K, "sourceConfig")
    applyBehavior(new PutSingleConfiguration(sourceConfigurationObject, sourceConfiguration))

    // 1.2.2 JsonDecoder-Filter Configuration
    val jsonDecoderConfiguration = loadJson(CONFIGURATIONS, K2K, "jsonDecoderConfig")
    val jsonDecoderConfigurationObject = loadConfiguration(K2K, "jsonDecoderConfig")
    applyBehavior(new PutSingleConfiguration(jsonDecoderConfigurationObject, jsonDecoderConfiguration))

    // 1.2.3 RemoveFields-Filter Configuration
    val removeFieldsConfiguration = loadJson(CONFIGURATIONS, K2K, "removeFieldsConfig")
    val removeFieldsConfigurationObject = loadConfiguration(K2K, "removeFieldsConfig")
    applyBehavior(new PutSingleConfiguration(removeFieldsConfigurationObject, removeFieldsConfiguration))

    // 1.2.4 JsonEncoder-Filter Configuration
    val jsonEncoderConfiguration = loadJson(CONFIGURATIONS, K2K, "jsonEncoderConfig")
    val jsonEncoderConfigurationObject = loadConfiguration(K2K, "jsonEncoderConfig")
    applyBehavior(new PutSingleConfiguration(jsonEncoderConfigurationObject, jsonEncoderConfiguration))

    // 1.2.5 Kafka-Sink Configuration
    val sinkConfiguration = loadJson(CONFIGURATIONS, K2K, "sinkConfig")
    val sinkConfigurationObject = loadConfiguration(K2K, "sinkConfig")
    applyBehavior(new PutSingleConfiguration(sinkConfigurationObject, sinkConfiguration))

    // 1.3 Start the K2K Pipeline
    val pipelineRefString = write(pipelineObject.ref)
    applyBehavior(new PipelineStart(pipelineObject, pipelineRefString))
  }

  private def creatingKafkaToElasticPipeline(implicit runner: TestRunner): TestAction = {
    import runner.applyBehavior

    // 2.1.1 Kafka-Source Blueprint
    val sourceBlueprint = loadJson(BLUEPRINTS, K2E, "sourceBlueprint")
    val sourceObject = loadSourceBlueprint(K2E, "sourceBlueprint")
    applyBehavior(new PutSingleBlueprint(sourceObject, sourceBlueprint))

    // 2.1.2 JsonDecoder-Filter Blueprint
    val jsonDecoderBlueprint = loadJson(BLUEPRINTS, K2E, "jsonDecoderBlueprint")
    val jsonDecoderBlueprintObject = loadFilterBlueprint(K2E, "jsonDecoderBlueprint")
    applyBehavior(new PutSingleBlueprint(jsonDecoderBlueprintObject, jsonDecoderBlueprint))

    // 2.1.3 RemoveFields-Filter Blueprint
    val removeFieldsBlueprint = loadJson(BLUEPRINTS, K2E, "removeFieldsBlueprint")
    val removeFieldsObject = loadFilterBlueprint(K2E, "removeFieldsBlueprint")
    applyBehavior(new PutSingleBlueprint(removeFieldsObject, removeFieldsBlueprint))

    // 2.1.4 Elastic-Sink Blueprint
    val sinkBlueprint = loadJson(BLUEPRINTS, K2E, "sinkBlueprint")
    val sinkObject = loadSinkBlueprint(K2E, "sinkBlueprint")
    applyBehavior(new PutSingleBlueprint(sinkObject, sinkBlueprint))

    // 2.1.5 Pipeline Blueprint
    val pipelineBlueprint = loadJson(BLUEPRINTS, K2E, "pipelineBlueprint")
    val pipelineObject = loadPipelineBlueprint(K2E, "pipelineBlueprint")
    putSinglePipelineBlueprint(pipelineObject, pipelineBlueprint)

    // 2.2.1 Kafka-Source Configuration
    val sourceConfiguration = loadJson(CONFIGURATIONS, K2E, "sourceConfig")
    val sourceConfigurationObject = loadConfiguration(K2E, "sourceConfig")
    applyBehavior(new PutSingleConfiguration(sourceConfigurationObject, sourceConfiguration))

    // 2.2.2 JsonDecoder-Filter Configuration
    val jsonDecoderConfiguration = loadJson(CONFIGURATIONS, K2E, "jsonDecoderConfig")
    val jsonDecoderConfigurationObject = loadConfiguration(K2E, "jsonDecoderConfig")
    applyBehavior(new PutSingleConfiguration(jsonDecoderConfigurationObject, jsonDecoderConfiguration))

    // 2.2.3 RemoveFields-Filter Configuration
    val removeFieldsConfiguration = loadJson(CONFIGURATIONS, K2E, "removeFieldsConfig")
    val removeFieldsConfigurationObject = loadConfiguration(K2E, "removeFieldsConfig")
    applyBehavior(new PutSingleConfiguration(removeFieldsConfigurationObject, removeFieldsConfiguration))

    // 2.2.4 Elastic-Sink Configuration
    val sinkConfiguration = loadJson(CONFIGURATIONS, K2E, "sinkConfig")
    val sinkConfigurationObject = loadConfiguration(K2E, "sinkConfig")
    applyBehavior(new PutSingleConfiguration(sinkConfigurationObject, sinkConfiguration))


    // 2.3 Start the K2E Pipeline
    val pipelineRefString = write(pipelineObject.ref)
    applyBehavior(new PipelineStart(pipelineObject, pipelineRefString))
  }

  private def putSinglePipelineBlueprint(pipelineObject: PipelineBlueprint, pipelineJSON: String)(implicit runner: TestRunner): Unit = {
    import runner.applyBehavior

    applyBehavior(new PutSinglePipelineBlueprint(pipelineObject, pipelineJSON))

    pipelineBlueprintsCount = pipelineBlueprintsCount + 1
  }

  private def checkAllPipelineBlueprints(expected: Int)(implicit runner: TestRunner, frontierClient: HttpClient, logger: Logger): Assertion = {
    val pipelineBlueprints = getAllPipelineBlueprints(runner, frontierClient, logger)

    pipelineBlueprints should have size expected
  }

  private def checkSinglePipelineBlueprint(pipelineObject: PipelineBlueprint)(implicit runner: TestRunner): Assertion = {

    val pipelineBlueprint = getSinglePipelineBlueprint(pipelineObject.ref.uuid)

    pipelineBlueprint.ref.uuid should equal(pipelineObject.ref.uuid)
  }

  private def checkFilterState(filterId: String, health: Health, status: FilterStatus)(implicit runner: TestRunner): Assertion = {

    val state = getFilterState(filterId)

    state.health shouldBe health
    state.status shouldBe status
  }

  private def checkExtractedDatasets(filterId: String, amount: Int, expect: Int)(implicit runner: TestRunner): Assertion = {

    val datasets = extractDatasets(filterId, amount)

    datasets should have size expect
  }

  private def cleanIntegrationTest(implicit runner: TestRunner): Assertion = {
    import runner.applyBehavior

    applyBehavior(new ElasticRemoveIndex("test")(runner, client = elasticClient, logger))
    checkAllPipelineBlueprints(pipelineBlueprintsCount)
    cleanUp
    pipelineBlueprintsCount = 0
    checkAllPipelineBlueprints(pipelineBlueprintsCount)
  }

}
package io.logbee.keyscore.test.integrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.test.fixtures.ExampleData._
import io.logbee.keyscore.test.integrationTests.behaviors._
import io.logbee.keyscore.test.util.JsonData._
import io.logbee.keyscore.test.util.TestData._
import io.logbee.keyscore.test.util.TestingMethods._
import org.json4s.Formats
import org.json4s.native.Serialization.write
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps

/**
  * This Citrus Integration-Test should ensure that all necessary '''data passes through''' a complete pipeline. <br><br>
  *
  * Therefor 3 Datasets with __one__ Record and __multiple__ Fields are inserted into the pipeline and are checked after they passed the __last__ Filter.
  */
@ExtendWith(value = Array(classOf[CitrusExtension]))
class WorkflowTest extends Matchers {

  implicit private val formats: Formats = KeyscoreFormats.formats

  implicit private val logger: Logger = LoggerFactory.getLogger(classOf[WorkflowTest])

  implicit private val client: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  //From the belonging JSONs
  val retainFieldsID = "f368c58c-db9a-43dc-8ccb-f495d29c441f"
  val secondRemoveFieldsID = "921a7d13-ebe0-49f8-8fc6-1e9064d1eba9"

  @Test
  @CitrusTest
  def runWorkflowTest(implicit @CitrusResource runner: TestRunner): Unit = {
    import runner.applyBehavior

    logger.debug(s"STARTING WorkflowTest")
    cleanUp

    logger.debug(s"CREATING Workflow Pipeline")
    createWorkflowPipeline(runner, client, logger)

    logger.debug(s"LOOKING_UP HealthState of the Workflow Pipeline")
    pollPipelineHealthState() shouldBe true

    logger.debug(s"INSERTING Datasets into the Workflow Pipeline")
    applyBehavior(new InsertDatasets(retainFieldsID, write(List(workflowFirstDataset, workflowSecondDataset, workflowThirdDataset))))

    logger.debug(s"CHECKING Datasets of the Workflow Pipeline")
    checkDatasets(filterID = secondRemoveFieldsID, f = checkWorkflowDatasets, amount = 3, expect = 3) shouldBe true

    Thread.sleep(5000)

    logger.debug(s"SCRAPING the metrics of the Workflow Pipeline")
    scrapeMetrics(id = secondRemoveFieldsID, mq = write(standardTimestamp)).last.metrics shouldNot be (empty)

    logger.debug(s"CLEANING_UP the Workflow Pipeline")
    cleanUp

    logger.debug(s"FINISHING WorkflowTest")
  }

  private def createWorkflowPipeline(implicit runner: TestRunner, client: HttpClient, logger: Logger): TestAction = {
    import runner.applyBehavior

    //1. KafkaSource Blueprint
    val kafkaSourceBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "kafkaSourceBlueprint")
    val kafkaSourceBlueprint = loadSourceBlueprint(WORKFLOW, "kafkaSourceBlueprint")
    applyBehavior(new PutSingleBlueprint(kafkaSourceBlueprint, kafkaSourceBlueprintJSON))
    //2. RetainFields Blueprint
    val retainFieldsBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "retainFieldsBlueprint")
    val retainFieldsBlueprint = loadFilterBlueprint(WORKFLOW, "retainFieldsBlueprint")
    applyBehavior(new PutSingleBlueprint(retainFieldsBlueprint, retainFieldsBlueprintJSON))
    //3. First RemoveFields Blueprint
    val firstRemoveFieldsBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "firstRemoveFieldsBlueprint")
    val firstRemoveFieldsBlueprint = loadFilterBlueprint(WORKFLOW, "firstRemoveFieldsBlueprint")
    applyBehavior(new PutSingleBlueprint(firstRemoveFieldsBlueprint, firstRemoveFieldsBlueprintJSON))
    //4. Second RemoveFields Blueprint
    val secondRemoveFieldsBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "secondRemoveFieldsBlueprint")
    val secondRemoveFieldsBlueprint = loadFilterBlueprint(WORKFLOW, "secondRemoveFieldsBlueprint")
    applyBehavior(new PutSingleBlueprint(secondRemoveFieldsBlueprint, secondRemoveFieldsBlueprintJSON))
    //5. ElasticSink Blueprint
    val elasticSinkBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "elasticSinkBlueprint")
    val elasticSinkBlueprint = loadSinkBlueprint(WORKFLOW, "elasticSinkBlueprint")
    applyBehavior(new PutSingleBlueprint(elasticSinkBlueprint, elasticSinkBlueprintJSON))
    //6. Pipeline Blueprint
    val workflowPipelineBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "workflowPipelineBlueprint")
    val workflowPipelineBlueprint = loadPipelineBlueprint(WORKFLOW, "workflowPipelineBlueprint")
    applyBehavior(new PutSinglePipelineBlueprint(workflowPipelineBlueprint, workflowPipelineBlueprintJSON))

    //1. KafkaSource Configuration
    val kafkaSourceConfigurationJSON = loadJson(CONFIGURATIONS, WORKFLOW, "kafkaSourceConfiguration")
    val kafkaSourceConfiguration = loadConfiguration(WORKFLOW, "kafkaSourceConfiguration")
    applyBehavior(new PutSingleConfiguration(kafkaSourceConfiguration, kafkaSourceConfigurationJSON))
    //2. RetainFields Configuration
    val retainFieldsConfigurationJSON = loadJson(CONFIGURATIONS, WORKFLOW, "retainFieldsConfiguration")
    val retainFieldsConfiguration = loadConfiguration(WORKFLOW, "retainFieldsConfiguration")
    applyBehavior(new PutSingleConfiguration(retainFieldsConfiguration, retainFieldsConfigurationJSON))
    //3. First RemoveFields Configuration
    val firstRemoveFieldsConfigurationJSON = loadJson(CONFIGURATIONS, WORKFLOW, "firstRemoveFieldsConfiguration")
    val firstRemoveFieldsConfiguration = loadConfiguration(WORKFLOW, "firstRemoveFieldsConfiguration")
    applyBehavior(new PutSingleConfiguration(firstRemoveFieldsConfiguration, firstRemoveFieldsConfigurationJSON))
    //4. Second RemoveFields Configuration
    val secondRemoveFieldsConfigurationJSON = loadJson(CONFIGURATIONS, WORKFLOW, "secondRemoveFieldsConfiguration")
    val secondRemoveFieldsConfiguration = loadConfiguration(WORKFLOW, "secondRemoveFieldsConfiguration")
    applyBehavior(new PutSingleConfiguration(secondRemoveFieldsConfiguration, secondRemoveFieldsConfigurationJSON))
    //5. ElasticSink Configuration
    val elasticSinkConfigurationJson = loadJson(CONFIGURATIONS, WORKFLOW, "elasticSinkConfiguration")
    val elasticSinkConfiguration = loadConfiguration(WORKFLOW, "elasticSinkConfiguration")
    applyBehavior(new PutSingleConfiguration(elasticSinkConfiguration, elasticSinkConfigurationJson))

    //Start the Pipeline
    val pipelineID: String = write(workflowPipelineBlueprint.ref)
    applyBehavior(new PipelineStart(workflowPipelineBlueprint, pipelineID))
  }

  private def checkWorkflowDatasets(dataset: Dataset): Assertion = {

    dataset.records should have size 1
    dataset.records.head.fields should have size 2
    val fieldNames = dataset.records.head.fields.map(field => field.name)
    fieldNames should contain ("text3")
    fieldNames should contain ("number1")
    fieldNames should not contain "text1"
    fieldNames should not contain "text2"
    fieldNames should not contain "number2"
    fieldNames should not contain "health"
    fieldNames should not contain "wanted"

  }
}

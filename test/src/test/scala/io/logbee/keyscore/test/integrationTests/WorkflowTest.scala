package io.logbee.keyscore.test.integrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.JsonData._
import io.logbee.keyscore.model.PipelineInstance
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.test.integrationTests.behaviors._
import org.json4s.native.Serialization.{read, write}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.{Assertion, Matchers}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.HttpStatus

import scala.concurrent.duration._

@ExtendWith(value = Array(classOf[CitrusExtension]))
class WorkflowTest extends Matchers {

  implicit private val formats = KeyscoreFormats.formats

  implicit private val logger = LoggerFactory.getLogger(classOf[WorkflowTest])

  implicit private val client: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  //Records and Datasets
  val workflowFirstRecord = Record(
    Field("text1", TextValue("01text1")),
    Field("text2", TextValue("01text2")),
    Field("text3", TextValue("01text3")),
    Field("number1", NumberValue(1001L)),
    Field("number2", NumberValue(1002L)),
    Field("wanted", BooleanValue(false)),
    Field("health", HealthValue(Health.Green))
  )

  val workflowSecondRecord = Record(
    Field("text1", TextValue("02text1")),
    Field("text2", TextValue("02text2")),
    Field("text3", TextValue("02text3")),
    Field("number1", NumberValue(2001L)),
    Field("number2", NumberValue(2002L)),
    Field("wanted", BooleanValue(true)),
    Field("health", HealthValue(Health.Yellow))
  )

  val workflowThirdRecord = Record(
    Field("text1", TextValue("03text1")),
    Field("text2", TextValue("03text2")),
    Field("text3", TextValue("03text3")),
    Field("number1", NumberValue(3001L)),
    Field("number2", NumberValue(3002L)),
    Field("wanted", BooleanValue(false)),
    Field("health", HealthValue(Health.Red))
  )

  val workflowFirstDataset = Dataset(workflowFirstRecord)
  val workflowSecondDataset = Dataset(workflowSecondRecord)
  val workflowThirdDataset = Dataset(workflowThirdRecord)

  val retainFieldsID = "f368c58c-db9a-43dc-8ccb-f495d29c441f"
  val secondRemoveFieldsID = "921a7d13-ebe0-49f8-8fc6-1e9064d1eba9"

  @Test
  @CitrusTest
  def testWorkflow(implicit @CitrusResource runner: TestRunner): Unit = {
    import runner.applyBehavior

    logger.debug(s"STARTING WorkflowTest")

    logger.debug(s"CREATING Workflow Pipeline")
    createWorkflowPipeline(runner,client,logger)

    logger.debug(s"LOOKING_UP HealthState of the Workflow Pipeline")
    pollPipelineHealthState() shouldBe true

    logger.debug(s"INSERTING Datasets into the Workflow Pipeline")
    applyBehavior(new InsertDatasets(retainFieldsID, write(List(workflowFirstDataset, workflowSecondDataset, workflowThirdDataset))))

    logger.debug(s"CHECKING Datasets of the Workflow Pipeline")
    pollDatasets(filterID = secondRemoveFieldsID, expect = 3) shouldBe true

    logger.debug(s"SCRAPING the metrics of the Workflow Pipeline")
    scrapeMetrics(filterID = secondRemoveFieldsID)

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
    applyBehavior(new StartPipeline(workflowPipelineBlueprint, pipelineID))
  }

  private def cleanUp(implicit runner: TestRunner, client: HttpClient, logger: Logger): Unit = {
    import runner._

    applyBehavior(new DeleteAllBlueprints())
    applyBehavior(new DeleteAllConfigurations())
    applyBehavior(new DeleteAllPipelines())

  }

  private def scrapeMetrics(filterID: String)(implicit runner: TestRunner): Assertion = {
    logger.debug(s"SCRAPE metrics for Filter <${filterID}>")

    var metrics = MetricsCollection()

    runner.http(action => action.client(client)
      .send()
      .get(s"/filter/${filterID}/scrape")
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, _) => {
        metrics = read[MetricsCollection](message.getPayload.asInstanceOf[String])
      })
    )

    metrics.metrics shouldNot be (empty)
  }

  private def pollPipelineHealthState(maxRetries: Int = 10, interval: FiniteDuration = 2 seconds, expect: Int = 1)(implicit runner: TestRunner): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      logger.debug(s"CHECK Health State for ${expect} Pipelines with $retries retries remaining.")
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

    runner.http(action => action.client(client)
      .send()
      .get(s"pipeline/instance/*")
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, _) => {
        val payload = message.getPayload.asInstanceOf[String]
        instances = read[List[PipelineInstance]](payload)
      }))

    instances
  }

  private def pollDatasets(filterID: String, expect: Int = 1, maxRetries: Int = 10, interval: FiniteDuration = 2 seconds)(implicit runner: TestRunner): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      logger.debug(s"Check Datasets for ${expect} Filter with $retries retries remaining.")

      val listOfDatasets = extractDatasetsFromFilter(filterID, amount = expect)

      if (listOfDatasets.size == expect) {
        listOfDatasets.foreach(dataset => {
          dataset.records should have size 1
          dataset.records.head.fields should have size 2
          val fieldNames = dataset.records.head.fields.map(field => field.name)
          fieldNames should contain("text3")
          fieldNames should contain("number1")
          fieldNames should not contain ("text1")
          fieldNames should not contain ("text2")
          fieldNames should not contain ("number2")
          fieldNames should not contain ("health")
          fieldNames should not contain ("wanted")
        })
        return true
      }

      Thread.sleep(interval.toMillis)
      retries -= 1
    }

    false
  }

  private def extractDatasetsFromFilter(filterID: String, amount: Int)(implicit runner: TestRunner): List[Dataset] = {
    logger.debug(s"EXTRACT Datasets for <${filterID}>")
    var listOfDatasets = List.empty[Dataset]

    runner.http(action => action.client(client)
      .send()
      .get(s"/filter/${filterID}/extract?value=" + amount)
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, _) => {
        listOfDatasets = read[List[Dataset]](message.getPayload.asInstanceOf[String])
      })
    )

    listOfDatasets
  }
  

}

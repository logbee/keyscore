package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import io.logbee.keyscore.JsonData._
import io.logbee.keyscore.model.PipelineInstance
import io.logbee.keyscore.model.blueprint.{PipelineBlueprint, SealedBlueprint}
import io.logbee.keyscore.model.configuration.Configuration
import org.json4s.native.Serialization.{read, write}
import org.springframework.http.HttpStatus
import io.logbee.keyscore.model.blueprint.ToBase.sealedToBase
import io.logbee.keyscore.model.configuration.ParameterMessage.SealedValue
import io.logbee.keyscore.model.data.Health.Green
import org.scalatest.Matchers

import scala.concurrent.duration._

@ExtendWith(value = Array(classOf[CitrusExtension]))
class WorkflowTest extends Matchers {

  private implicit val formats = KeyscoreFormats.formats
  private val logger = LoggerFactory.getLogger(classOf[WorkflowTest])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  private val elasticClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:9200")
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
    createWorkflowPipeline(runner)
    pollPipelineHealthState() shouldBe true
    //Insert Datasets
    insertDatasetsIntoFilter(retainFieldsID, write(List(workflowFirstDataset, workflowSecondDataset, workflowThirdDataset)))
    //Check Datasets
    pollDatasets(filterID = secondRemoveFieldsID, expect = 3) shouldBe true
    //Cleanup
    cleanUp
  }

  private def createWorkflowPipeline(implicit runner: TestRunner): TestAction = {
    //1. KafkaSource Blueprint
    val kafkaSourceBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "kafkaSourceBlueprint")
    val kafkaSourceBlueprint = loadSourceBlueprint(WORKFLOW, "kafkaSourceBlueprint")
    putSingleBlueprint(kafkaSourceBlueprint, kafkaSourceBlueprintJSON)
    //2. RetainFields Blueprint
    val retainFieldsBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "retainFieldsBlueprint")
    val retainFieldsBlueprint = loadFilterBlueprint(WORKFLOW, "retainFieldsBlueprint")
    putSingleBlueprint(retainFieldsBlueprint, retainFieldsBlueprintJSON)
    //3. First RemoveFields Blueprint
    val firstRemoveFieldsBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "firstRemoveFieldsBlueprint")
    val firstRemoveFieldsBlueprint = loadFilterBlueprint(WORKFLOW, "firstRemoveFieldsBlueprint")
    putSingleBlueprint(firstRemoveFieldsBlueprint, firstRemoveFieldsBlueprintJSON)
    //4. Second RemoveFields Blueprint
    val secondRemoveFieldsBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "secondRemoveFieldsBlueprint")
    val secondRemoveFieldsBlueprint = loadFilterBlueprint(WORKFLOW, "secondRemoveFieldsBlueprint")
    putSingleBlueprint(secondRemoveFieldsBlueprint, secondRemoveFieldsBlueprintJSON)
    //5. ElasticSink Blueprint
    val elasticSinkBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "elasticSinkBlueprint")
    val elasticSinkBlueprint = loadSinkBlueprint(WORKFLOW, "elasticSinkBlueprint")
    putSingleBlueprint(elasticSinkBlueprint, elasticSinkBlueprintJSON)
    //6. Pipeline Blueprint
    val workflowPipelineBlueprintJSON = loadJson(BLUEPRINTS, WORKFLOW, "workflowPipelineBlueprint")
    val workflowPipelineBlueprint = loadPipelineBlueprint(WORKFLOW, "workflowPipelineBlueprint")
    putSinglePipelineBlueprint(workflowPipelineBlueprint, workflowPipelineBlueprintJSON)

    //1. KafkaSource Configuration
    val kafkaSourceConfigurationJSON = loadJson(CONFIGURATIONS, WORKFLOW, "kafkaSourceConfiguration")
    val kafkaSourceConfiguration = loadConfiguration(WORKFLOW, "kafkaSourceConfiguration")
    putSingleConfiguration(kafkaSourceConfiguration, kafkaSourceConfigurationJSON)
    //2. RetainFields Configuration
    val retainFieldsConfigurationJSON = loadJson(CONFIGURATIONS, WORKFLOW, "retainFieldsConfiguration")
    val retainFieldsConfiguration = loadConfiguration(WORKFLOW, "retainFieldsConfiguration")
    putSingleConfiguration(retainFieldsConfiguration, retainFieldsConfigurationJSON)
    //3. First RemoveFields Configuration
    val firstRemoveFieldsConfigurationJSON = loadJson(CONFIGURATIONS, WORKFLOW, "firstRemoveFieldsConfiguration")
    val firstRemoveFieldsConfiguration = loadConfiguration(WORKFLOW, "firstRemoveFieldsConfiguration")
    putSingleConfiguration(firstRemoveFieldsConfiguration, firstRemoveFieldsConfigurationJSON)
    //4. Second RemoveFields Configuration
    val secondRemoveFieldsConfigurationJSON = loadJson(CONFIGURATIONS, WORKFLOW, "secondRemoveFieldsConfiguration")
    val secondRemoveFieldsConfiguration = loadConfiguration(WORKFLOW, "secondRemoveFieldsConfiguration")
    putSingleConfiguration(secondRemoveFieldsConfiguration, secondRemoveFieldsConfigurationJSON)
    //5. ElasticSink Configuration
    val elasticSinkConfigurationJson = loadJson(CONFIGURATIONS, WORKFLOW, "elasticSinkConfiguration")
    val elasticSinkConfiguration = loadConfiguration(WORKFLOW, "elasticSinkConfiguration")
    putSingleConfiguration(elasticSinkConfiguration, elasticSinkConfigurationJson)

    //startPipeline
    val pipelineID: String = write(workflowPipelineBlueprint.ref)
    startPipeline(workflowPipelineBlueprint, pipelineID)
  }

  private def cleanUp(implicit runner: TestRunner): TestAction = {
    deleteBlueprints
    deleteConfigurations
    deletePipelines
  }

  private def putSinglePipelineBlueprint(pipelineObject: PipelineBlueprint, pipelineJSON: String)(implicit runner: TestRunner): TestAction = {
    logger.debug(s"Reached PUT PipelineBlueprint for ${pipelineObject.ref.uuid}")

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

  private def putSingleBlueprint(blueprintObject: SealedBlueprint, pipelineJSON: String)(implicit runner: TestRunner): TestAction = {
    logger.debug(s"PUT Blueprint")

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

  private def putSingleConfiguration(configurationObject: Configuration, configurationJSON: String)(implicit runner: TestRunner): TestAction = {
    logger.debug(s"PUT Configuration for ${configurationObject.ref.uuid}")

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

  private def startPipeline(pipelineObject: PipelineBlueprint, pipelineID: String)(implicit runner: TestRunner): TestAction = {
    logger.debug(s"Start Pipeline for ${pipelineObject.ref.uuid}")

    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/pipeline/blueprint")
      .contentType("application/json")
      .payload(pipelineID)
    )

  }

  private def pollPipelineHealthState(maxRetries: Int = 10, interval: FiniteDuration = 2 seconds, expect: Int = 1)(implicit runner: TestRunner): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      logger.debug(s"Check Health State for ${expect} Pipelines with $retries retries remaining.")
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
      .validationCallback((message, _) => {
        val payload = message.getPayload.asInstanceOf[String]
        instances = read[List[PipelineInstance]](payload)
      }))

    instances
  }

  private def insertDatasetsIntoFilter(filterId: String, datasets: String)(implicit runner: TestRunner): TestAction = {
    logger.debug(s"Reached Insert Dataset for ${filterId} with ${datasets}")

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

  private def pollDatasets(filterID: String, expect: Int = 1, maxRetries: Int = 10, interval: FiniteDuration = 2 seconds)(implicit runner: TestRunner): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      logger.debug(s"Check Datasets for ${expect} Filter with $retries retries remaining.")

      val listOfDatasets = extractDatsetsFromFilter(filterID, amount = expect)

      if (listOfDatasets.size == expect) {
        listOfDatasets.foreach(dataset => {
          dataset.records should have size 1
          dataset.records.head.fields should have size 2
          val fieldNames = dataset.records.head.fields.map(field => field.name)
          fieldNames should contain ("text3")
          fieldNames should contain ("number1")
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

  private def extractDatsetsFromFilter(filterId: String, amount: Int)(implicit runner: TestRunner): List[Dataset] = {
    logger.debug(s"Reached Extract Datasets for ${filterId}")
    var listOfDatasets = List.empty[Dataset]

    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"/filter/${filterId}/extract?value=" + amount)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, _) => {
        listOfDatasets = read[List[Dataset]](message.getPayload.asInstanceOf[String])
      })
    )

    listOfDatasets
  }

  private def deleteConfigurations(implicit runner: TestRunner): TestAction = {
    logger.debug(s"Deleting all configurations")

    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"/resources/configuration/*")
    )
  }

  private def deleteBlueprints(implicit runner: TestRunner): TestAction = {
    logger.debug(s"Deleting all blueprints")

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
    logger.debug(s"Deleting all pipelines")

    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"/pipeline/blueprint/*")
    )
  }

}

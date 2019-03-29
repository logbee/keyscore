package io.logbee.keyscore.test.integrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.PipelineInstance
import io.logbee.keyscore.model.blueprint.ToBase.sealedToBase
import io.logbee.keyscore.model.blueprint.{PipelineBlueprint, SealedBlueprint}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.test.util.JsonData._
import org.json4s.native.Serialization.{read, write}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

import scala.concurrent.duration._

/*********************************************
* Comment out all annotations before pushing *
**********************************************/

@ExtendWith(value = Array(classOf[CitrusExtension]))
class SetupTest extends Matchers {

  private implicit val formats = KeyscoreFormats.formats
  private val logger = LoggerFactory.getLogger(classOf[SetupTest])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  /*********************************
  * Create the Evaluation Pipeline *
  **********************************/

  @Test
  @CitrusTest
  def testSetup(implicit @CitrusResource runner: TestRunner): Unit = {
    createTestSetup(runner)
  }

  /*************************************************
  * Run this after the tests are finished *
  **************************************************/

  @Test
  @CitrusTest
  def cleanUpTestSetup(implicit @CitrusResource runner: TestRunner): Unit = {
    cleanUp(runner)
  }


  /*****************************
  * Create the Helper Pipeline *
  ******************************/

  @Test
  @CitrusTest
  def fillKafkaSetup(implicit @CitrusResource runner: TestRunner): Unit = {
    fillKafka(runner)
  }

  private def createTestSetup(implicit runner: TestRunner): TestAction = {
    //1. KafkaSource Blueprint
    val kafkaSourceBlueprintJSON = loadJson(BLUEPRINTS, TS, "kafkaSourceBlueprint")
    val kafkaSourceBlueprint = loadSourceBlueprint(TS, "kafkaSourceBlueprint")
    putSingleBlueprint(kafkaSourceBlueprint, kafkaSourceBlueprintJSON)
    //2. RemoveFields Blueprint
    val removeFieldsBlueprintJSON = loadJson(BLUEPRINTS, TS, "removeFieldsBlueprint")
    val removeFieldsBlueprint = loadFilterBlueprint(TS, "removeFieldsBlueprint")
    putSingleBlueprint(removeFieldsBlueprint, removeFieldsBlueprintJSON)
    //3. Grok Blueprint
    val grokBlueprintJSON = loadJson(BLUEPRINTS, TS, "grokBlueprint")
    val grokBlueprint = loadFilterBlueprint(TS, "grokBlueprint")
    putSingleBlueprint(grokBlueprint, grokBlueprintJSON)
    //4. Fingerprint Blueprint
    val fingerprintBlueprintJSON = loadJson(BLUEPRINTS, TS, "fingerprintBlueprint")
    val fingerprintBlueprint = loadFilterBlueprint(TS, "fingerprintBlueprint")
    putSingleBlueprint(fingerprintBlueprint, fingerprintBlueprintJSON)
    //5. KafkaSink Blueprint
    val kafkaSinkBlueprintJSON = loadJson(BLUEPRINTS, TS, "kafkaSinkBlueprint")
    val kafkaSinkBlueprint = loadSinkBlueprint(TS, "kafkaSinkBlueprint")
    putSingleBlueprint(kafkaSinkBlueprint, kafkaSinkBlueprintJSON)
    //6. TestSetup Blueprint
    val testSetupBlueprintJSON = loadJson(BLUEPRINTS, TS, "testSetupBlueprint")
    val testSetupBlueprint = loadPipelineBlueprint(TS, "testSetupBlueprint")
    putSinglePipelineBlueprint(testSetupBlueprint, testSetupBlueprintJSON)

    //1. KafkaSource Configuration
    val kafkaSourceConfigurationJSON = loadJson(CONFIGURATIONS, TS, "kafkaSourceConfiguration")
    val kafkaSourceConfiguration = loadConfiguration(TS, "kafkaSourceConfiguration")
    putSingleConfiguration(kafkaSourceConfiguration, kafkaSourceConfigurationJSON)
    //2. RemoveFields Configuration
    val removeFieldsConfigurationJSON = loadJson(CONFIGURATIONS, TS, "removeFieldsConfiguration")
    val removeFieldsConfiguration = loadConfiguration(TS, "removeFieldsConfiguration")
    putSingleConfiguration(removeFieldsConfiguration, removeFieldsConfigurationJSON)
    //3. Grok Configuration
    val grokConfigurationJSON = loadJson(CONFIGURATIONS, TS, "grokConfiguration")
    val grokConfiguration = loadConfiguration(TS, "grokConfiguration")
    putSingleConfiguration(grokConfiguration, grokConfigurationJSON)
    //4. Fingerprint Configuration
    val fingerprintConfigurationJSON = loadJson(CONFIGURATIONS, TS, "fingerprintConfiguration")
    val fingerprintConfiguration = loadConfiguration(TS, "fingerprintConfiguration")
    putSingleConfiguration(fingerprintConfiguration, fingerprintConfigurationJSON)
    //5. KafkaSink Configuration
    val kafkaSinkConfigurationJSON = loadJson(CONFIGURATIONS, TS, "kafkaSinkConfiguration")
    val kafkaSinkConfiugration = loadConfiguration(TS, "kafkaSinkConfiguration")
    putSingleConfiguration(kafkaSinkConfiugration, kafkaSinkConfigurationJSON)

    //Start TestSetup Pipeline
    val pipelineID: String = write(testSetupBlueprint.ref)
    startPipeline(testSetupBlueprint, pipelineID)
  }

  private def fillKafka(implicit runner: TestRunner): TestAction = {
    //1. KafkaSource Blueprint
    val kafkaSourceBlueprintJSON = loadJson(BLUEPRINTS, FK, "kafkaSourceBlueprint")
    val kafkaSourceBlueprint = loadSourceBlueprint(FK, "kafkaSourceBlueprint")
    putSingleBlueprint(kafkaSourceBlueprint, kafkaSourceBlueprintJSON)
    //2. KafkaSink Blueprint
    val kafkaSinkBlueprintJSON = loadJson(BLUEPRINTS, FK, "kafkaSinkBlueprint")
    val kafkaSinkBlueprint = loadSinkBlueprint(FK, "kafkaSinkBlueprint")
    putSingleBlueprint(kafkaSinkBlueprint, kafkaSinkBlueprintJSON)
    //3. Pipeline Blueprint
    val fillKafkaBlueprintJSON = loadJson(BLUEPRINTS, FK, "fillKafkaBlueprint")
    val fillKafkaBlueprint  =loadPipelineBlueprint(FK, "fillKafkaBlueprint")
    putSinglePipelineBlueprint(fillKafkaBlueprint, fillKafkaBlueprintJSON)

    //1. KafkaSource Configuration
    val kafkaSourceConfigurationJSON = loadJson(CONFIGURATIONS, FK, "kafkaSourceConfiguration")
    val kafkaSourceConfiguration = loadConfiguration(FK, "kafkaSourceConfiguration")
    putSingleConfiguration(kafkaSourceConfiguration, kafkaSourceConfigurationJSON)
    //2. KafkaSink Configuration
    val kafkaSinkConfigurationJSON = loadJson(CONFIGURATIONS, FK, "kafkaSinkConfiguration")
    val kafkaSinkConfiguration = loadConfiguration(FK, "kafkaSinkConfiguration")
    putSingleConfiguration(kafkaSinkConfiguration, kafkaSinkConfigurationJSON)

    //Start Pipeline
    val pipelineID: String = write(fillKafkaBlueprint.ref)
    startPipeline(fillKafkaBlueprint, pipelineID)
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
      .response(HttpStatus.OK)
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
      .response(HttpStatus.OK)
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
      .response(HttpStatus.OK)
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

  private def cleanUp(implicit runner: TestRunner): TestAction = {
    deleteBlueprints
    deleteConfigurations
    deletePipelines
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

package io.logbee.keyscore.test.integrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.agent.pipeline.valve.ValveStage._
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.metrics.GaugeMetric
import io.logbee.keyscore.model.pipeline.{Dismantled, FilterStatus, Paused, Running}
import io.logbee.keyscore.test.integrationTests.behaviors._
import io.logbee.keyscore.test.util.JsonData._
import io.logbee.keyscore.test.util.TestingMethods._
import org.json4s.Formats
import org.json4s.native.Serialization.write
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.{Assertion, Matchers}
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps

/**
  * This Citrus Integration-Test should ensure that '''Metrics''' from a __specific__ Filter can be collected. <br><br>
  *
  * In Future, this should __also__ check the new ''~MetricsCollector~'' .
  */
@ExtendWith(value = Array(classOf[CitrusExtension]))
class MetricsTest extends Matchers {

  implicit private val formats: Formats = KeyscoreFormats.formats

  implicit private val logger: Logger = LoggerFactory.getLogger(classOf[WorkflowTest])

  implicit private val client: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  private val d1 = Dataset(Record(Field("number", NumberValue(1L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))
  private val d2 = Dataset(Record(Field("number", NumberValue(2L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))
  private val d3 = Dataset(Record(Field("number", NumberValue(3L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))
  private val d4 = Dataset(Record(Field("number", NumberValue(4L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))
  private val d5 = Dataset(Record(Field("number", NumberValue(5L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))
  private val d6 = Dataset(Record(Field("number", NumberValue(6L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))
  private val d7 = Dataset(Record(Field("number", NumberValue(7L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))
  private val d8 = Dataset(Record(Field("number", NumberValue(8L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))
  private val d9 = Dataset(Record(Field("number", NumberValue(9L)), Field("text", TextValue("_")), Field("delete", BooleanValue(true))))

  val decoderID = "1d8d0973-d9ed-418c-917c-5f4c18fd51e7"
  val addFieldsID = "a2912661-7ce2-40d3-b490-d6c58a5cb70f"
  val retainID = "e3d82c2a-9bfd-4118-93aa-11c1b1dfaf82"
  val removeID = "34402c9c-09bb-4fc8-be8f-70a513ed6d66"
  val encoderID = "4f7ed3f3-b6b3-489c-ade6-f7cd09fb0197"

  @Test
  @CitrusTest
  def runMetricsTest(implicit @CitrusResource runner: TestRunner): Unit = {
    import runner.applyBehavior

    logger.debug(s"STARTING MetricsTest")

    logger.debug(s"CREATING Metrics Pipeline")
    createMetricsPipeline(runner, client, logger)

    logger.debug(s"LOOKING_UP HealthState of the Metrics Pipeline")
    pollPipelineHealthState() should be(true)

    logger.debug(s"At the beginning no datasets should be inserted or extracted.")
    scrapeMetrics(addFieldsID) find insertedDatasets should be(None)
    scrapeMetrics(addFieldsID) find extractedDatasets should be(None)

    logger.debug(s"Now 3 datasets should be inserted.")
    applyBehavior(new InsertDatasets(addFieldsID, write(List(d1, d2, d3))))
    (scrapeMetrics(addFieldsID) find insertedDatasets get).value shouldBe 3

    logger.debug(s"Also 3 datasets should been now pushed to the next filter.")
    (scrapeMetrics(addFieldsID) find pushedDatasets get).value shouldBe 3
    (scrapeMetrics(retainID) find pushedDatasets get).value shouldBe 3

    logger.debug(s"There still should be 3 datasets that were pushed to the next filter")
    applyBehavior(new FilterPause(retainID, "true"))
    checkFilterState(retainID, Green, Paused)
    applyBehavior(new FilterDrain(retainID, "true"))
    checkFilterState(retainID, Green, Dismantled)

    Thread.sleep(2000)

    applyBehavior(new InsertDatasets(retainID, write(List(d4, d5, d6))))
    (scrapeMetrics(retainID) find insertedDatasets get).value shouldBe 3
    (scrapeMetrics(retainID) find drainedDatasets get).value shouldBe 3
    (scrapeMetrics(retainID) find pushedDatasets get).value shouldBe 3

    applyBehavior(new FilterPause(retainID, "false"))
    applyBehavior(new FilterDrain(retainID, "false"))
    checkFilterState(retainID, Green, Running)
    (scrapeMetrics(retainID) find pushedDatasets get).value shouldBe 3
    scrapeMetrics(addFieldsID) find drainedDatasets should be(None)
    (scrapeMetrics(removeID) find drainedDatasets get).value should be(3)
    scrapeMetrics(encoderID) find drainedDatasets should be(None)


    logger.debug("From the last filter 6 datasets should have been extracted.")
    (scrapeMetrics(removeID) find pushedDatasets get).value shouldBe 3
    applyBehavior(new InsertDatasets(retainID, write(List(d7, d8, d9))))
    (scrapeMetrics(retainID) find insertedDatasets get).value shouldBe 6
    (scrapeMetrics(retainID) find pushedDatasets get).value shouldBe 6

    extractDatasets(removeID, 10).size shouldBe 6
    (scrapeMetrics(removeID) find extractedDatasets get).value shouldBe 6

    //The throughputTime can sometimes be flaky (0.0)
    logger.debug("The total throughputTime should increase over time.")
    applyBehavior(new InsertDatasets(decoderID, write(List(d1, d2, d3, d4, d5, d6, d7, d8, d9, d1, d2, d3, d4, d5, d6, d7, d8, d9))))

    Thread.sleep(7000)

    val firstIn  = scrapeMetrics(addFieldsID).find[GaugeMetric](_totalThroughputTime.name, Set(Label("port", TextValue("in")))).get.value
    val firstOut = scrapeMetrics(addFieldsID).find[GaugeMetric](_totalThroughputTime.name, Set(Label("port", TextValue("out")))).get.value
    val lastIn  = scrapeMetrics(removeID).find[GaugeMetric](_totalThroughputTime.name, Set(Label("port", TextValue("in")))).get.value
    val lastOut = scrapeMetrics(removeID).find[GaugeMetric](_totalThroughputTime.name, Set(Label("port", TextValue("out")))).get.value

    lastOut should be > 0.0
    lastIn should be <= lastOut

    firstIn should be < lastIn
    firstOut should be < lastOut

    logger.debug("CLEANING_UP the Metrics Pipeline")
    cleanUp

    logger.debug(s"FINISHING MetricsTest")
  }

  private def createMetricsPipeline(implicit runner: TestRunner, client: HttpClient, logger: Logger): TestAction = {
    import runner.applyBehavior

    // 1. KafkaSource Blueprint
    val kafkaSourceBlueprintJSON = loadJson(BLUEPRINTS, METRICS, "sourceBlueprint")
    val kafkaSourceBlueprint = loadSourceBlueprint(METRICS, "sourceBlueprint")
    applyBehavior(new PutSingleBlueprint(kafkaSourceBlueprint, kafkaSourceBlueprintJSON))
    // 2. JsonDecoder Blueprint
    val jsonDecoderBlueprint = loadJson(BLUEPRINTS, METRICS, "jsonDecoderBlueprint")
    val jsonDecoderBlueprintObject = loadFilterBlueprint(METRICS, "jsonDecoderBlueprint")
    applyBehavior(new PutSingleBlueprint(jsonDecoderBlueprintObject, jsonDecoderBlueprint))
    // 3. AddFields Blueprint
    val addFieldsBlueprintJSON = loadJson(BLUEPRINTS, METRICS, "addFieldsBlueprint")
    val addFieldsBlueprint = loadFilterBlueprint(METRICS, "addFieldsBlueprint")
    applyBehavior(new PutSingleBlueprint(addFieldsBlueprint, addFieldsBlueprintJSON))
    // 4. RetainFields Blueprint
    val retainFieldsBlueprintJSON = loadJson(BLUEPRINTS, METRICS, "retainFieldsBlueprint")
    val retainFieldsBlueprint = loadFilterBlueprint(METRICS, "retainFieldsBlueprint")
    applyBehavior(new PutSingleBlueprint(retainFieldsBlueprint, retainFieldsBlueprintJSON))
    // 5. RemoveFields Blueprint
    val removeFieldsBlueprintJSON = loadJson(BLUEPRINTS, METRICS, "removeFieldsBlueprint")
    val removeFieldsBlueprint = loadFilterBlueprint(METRICS, "removeFieldsBlueprint")
    applyBehavior(new PutSingleBlueprint(removeFieldsBlueprint, removeFieldsBlueprintJSON))
    // 6. JsonEncoder Blueprint
    val jsonEncoderBlueprint = loadJson(BLUEPRINTS, METRICS, "jsonEncoderBlueprint")
    val jsonEncoderBlueprintObject = loadFilterBlueprint(METRICS, "jsonEncoderBlueprint")
    applyBehavior(new PutSingleBlueprint(jsonEncoderBlueprintObject, jsonEncoderBlueprint))
    // 7. KafkaSink Blueprint
    val sinkBlueprintJSON = loadJson(BLUEPRINTS, METRICS, "sinkBlueprint")
    val sinkBlueprint = loadSinkBlueprint(METRICS, "sinkBlueprint")
    applyBehavior(new PutSingleBlueprint(sinkBlueprint, sinkBlueprintJSON))
    // 8. Pipeline Blueprint
    val pipelineBlueprintJSON = loadJson(BLUEPRINTS, METRICS, "pipelineBlueprint")
    val pipelineBlueprint = loadPipelineBlueprint(METRICS, "pipelineBlueprint")
    applyBehavior(new PutSinglePipelineBlueprint(pipelineBlueprint, pipelineBlueprintJSON))


    // 1. KafkaSource Configuration
    val sourceConfiguration = loadJson(CONFIGURATIONS, METRICS, "sourceConfig")
    val sourceConfigurationObject = loadConfiguration(METRICS, "sourceConfig")
    applyBehavior(new PutSingleConfiguration(sourceConfigurationObject, sourceConfiguration))
    // 2. JsonDecoder Configuration
    val jsonDecoderConfiguration = loadJson(CONFIGURATIONS, METRICS, "jsonDecoderConfig")
    val jsonDecoderConfigurationObject = loadConfiguration(METRICS, "jsonDecoderConfig")
    applyBehavior(new PutSingleConfiguration(jsonDecoderConfigurationObject, jsonDecoderConfiguration))
    // 3. AddFields Configuration
    val addFieldsConfiguration = loadJson(CONFIGURATIONS, METRICS, "addFieldsConfig")
    val addFieldsConfigurationObject = loadConfiguration(METRICS, "addFieldsConfig")
    applyBehavior(new PutSingleConfiguration(addFieldsConfigurationObject, addFieldsConfiguration))
    // 4. RetainFields Configuration
    val retainFieldsConfiguration = loadJson(CONFIGURATIONS, METRICS, "retainFieldsConfig")
    val retainFieldsConfigurationObject = loadConfiguration(METRICS, "retainFieldsConfig")
    applyBehavior(new PutSingleConfiguration(retainFieldsConfigurationObject, retainFieldsConfiguration))
    // 5. RemoveFields Configuration
    val removeFieldsConfiguration = loadJson(CONFIGURATIONS, METRICS, "removeFieldsConfig")
    val removeFieldsConfigurationObject = loadConfiguration(METRICS, "removeFieldsConfig")
    applyBehavior(new PutSingleConfiguration(removeFieldsConfigurationObject, removeFieldsConfiguration))
    // 6. JsonEncoder Configuration
    val jsonEncoderConfiguration = loadJson(CONFIGURATIONS, METRICS, "jsonEncoderConfig")
    val jsonEncoderConfigurationObject = loadConfiguration(METRICS, "jsonEncoderConfig")
    applyBehavior(new PutSingleConfiguration(jsonEncoderConfigurationObject, jsonEncoderConfiguration))
    // 7. KafkaSink Configuration
    val sinkConfiguration = loadJson(CONFIGURATIONS, METRICS, "sinkConfig")
    val sinkConfigurationObject = loadConfiguration(METRICS, "sinkConfig")
    applyBehavior(new PutSingleConfiguration(sinkConfigurationObject, sinkConfiguration))

    //Start the Pipeline
    val pipelineID: String = write(pipelineBlueprint.ref)
    applyBehavior(new PipelineStart(pipelineBlueprint, pipelineID))
  }

  private def checkFilterState(filterId: String, health: Health, status: FilterStatus)(implicit runner: TestRunner): Assertion = {

    val state = getFilterState(filterId)

    state.health shouldBe health
    state.status shouldBe status
  }

}
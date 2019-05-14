package io.logbee.keyscore.test.util

import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.PipelineInstance
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.model.pipeline.FilterState
import io.logbee.keyscore.test.integrationTests.behaviors.{DeleteAllBlueprints, DeleteAllConfigurations, DeleteAllPipelines}
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.read
import org.scalatest.Assertion
import org.slf4j.Logger
import org.springframework.http.HttpStatus

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Contains methods that are recurrently used in the Citrus Tests.
  */
object TestingMethods {

  implicit private val formats = KeyscoreFormats.formats

  private[test] def extractDatasets(filterID: String, amount: Int)(implicit runner: TestRunner, client: HttpClient, logger: Logger): List[Dataset] = {
    logger.debug(s"EXTRACT Datasets for <${filterID}>")
    var datasets = List.empty[Dataset]

    runner.http(action => action.client(client)
      .send()
      .get(s"/filter/${filterID}/extract?value=" + amount)
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, _) => {
        datasets = read[List[Dataset]](message.getPayload.asInstanceOf[String])
      })
    )

    datasets
  }

  private[test] def checkDatasets(filterID: String, f: Dataset => Assertion, amount: Int = 1, expect: Int = 1, maxRetries: Int = 10, interval: FiniteDuration = 2 seconds)(implicit runner: TestRunner, client: HttpClient, logger: Logger): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      logger.debug(s"Check Datasets for ${expect} Filter with $retries retries remaining.")

      val datasets = extractDatasets(filterID, amount = expect)

      if (datasets.size == expect) {
        datasets.foreach(dataset => f(dataset) )
        return true
      }

      Thread.sleep(interval.toMillis)
      retries -= 1
    }

    false
  }

  private[test] def getSinglePipelineBlueprint(blueprintID: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger): PipelineBlueprint = {
    logger.debug(s"GET PipelineBlueprint for <${blueprintID}>")

    var pipelineBlueprint = PipelineBlueprint()

    runner.http(action => action.client(client)
      .send()
      .get(s"resources/blueprint/pipeline/${blueprintID}")
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, _) => {
        val payload = message.getPayload().asInstanceOf[String]
        pipelineBlueprint = read[PipelineBlueprint](payload)
      })
    )

    pipelineBlueprint
  }

  private[test] def getAllPipelineBlueprints(implicit runner: TestRunner, client: HttpClient, logger: Logger): Map[BlueprintRef, PipelineBlueprint] = {
    logger.debug(s"GET_ALL PipelineBlueprints")

    var pipelineBlueprints = Map.empty[BlueprintRef, PipelineBlueprint]

    runner.http(action => action.client(client)
      .send()
      .get(s"resources/blueprint/pipeline/*")
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, _) => {
        val payload = message.getPayload().asInstanceOf[String]
        pipelineBlueprints = read[Map[BlueprintRef, PipelineBlueprint]](payload)
      })
    )

    pipelineBlueprints
  }

  private[test] def getFilterState(filterId: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger): FilterState = {
    logger.debug(s"GET Filter State for <${filterId}>")

    var state: FilterState = null

    runner.http(action => action.client(client)
      .send()
      .get(s"/filter/${filterId}/state")
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.ACCEPTED)
      .validationCallback((message, context) => {
        val payload = message.getPayload.asInstanceOf[String]
        state = read[FilterState](payload)
      })
    )
    state
  }

  private[test] def getAllPipelineInstances(implicit runner: TestRunner, client: HttpClient, logger: Logger): List[PipelineInstance] = {
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

  private[test] def pollPipelineHealthState(maxRetries: Int = 10, interval: FiniteDuration = 2 seconds, expect: Int = 1)(implicit runner: TestRunner, client: HttpClient, logger: Logger): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      logger.debug(s"CHECK Health State for ${expect} Pipelines with $retries retries remaining.")

      var greenInstances: Int = 0

      val instances = getAllPipelineInstances(runner, client, logger)

      instances.foreach(instance => {
        if (instance.health == Green) greenInstances += 1
      })

      if (greenInstances == expect) return true

      Thread.sleep(interval.toMillis)
      retries -= 1
    }

    false
  }

  private[test] def getElementsFromElasticTopic(topic: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger): Int = {
    logger.debug(s"GET Elements from ($topic) in elastic.")

    var hits: Int = -1

    runner.http(action => action.client(client)
      .send()
      .get(s"/$topic/_search")
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val response = message.getPayload.asInstanceOf[String]
        val json = parse(response)
        hits = (json \ "hits" \ "total").extract[Int]
      }))

    logger.debug(s"$hits hits for topic ($topic) in elastic.")
    hits
  }

  private[test] def pollElasticElements(topic: String, maxRetries: Int = 10, interval: FiniteDuration = 2 seconds, expect: Int)(implicit runner: TestRunner, client: HttpClient, logger: Logger): Boolean = {
    var retries = maxRetries
    while (retries > 0) {
      logger.info(s"POLL elastic in ($topic) for ${expect} elements with $retries retries remaining.")

      try {
        val elements = getElementsFromElasticTopic(topic)(runner, client, logger)
        if (elements == expect) {
          return true
        }
      }
      catch {
        case e: Throwable => logger.error("Something went wrong while polling elasticsearch.", e)
      }

      Thread.sleep(interval.toMillis)
      retries -= 1
    }

    false
  }

  private[test] def scrapeMetrics(id: String, mq: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger): Seq[MetricsCollection] = {
    logger.debug(s"Query metrics for <$id> with $mq")

    var metrics = Seq(MetricsCollection())

    runner.http(action => action.client(client)
      .send()
      .get(s"/filter/$id")
      .contentType("application/json")
      .payload(mq)
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, _) => {
        metrics = read[Seq[MetricsCollection]](message.getPayload.asInstanceOf[String])
      })
    )

    metrics
  }

  private[test] def cleanUp(implicit runner: TestRunner, client: HttpClient, logger: Logger): Unit = {
    import runner.applyBehavior

    applyBehavior(new DeleteAllBlueprints())
    applyBehavior(new DeleteAllConfigurations())
    applyBehavior(new DeleteAllPipelines())

  }

}

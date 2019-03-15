package io.logbee.keyscore.test.util

import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.PipelineInstance
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.test.integrationTests.behaviors.{DeleteAllBlueprints, DeleteAllConfigurations, DeleteAllPipelines}
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

  private[test] def pollDatasets(filterID: String, f: Dataset => Assertion, expect: Int = 1, maxRetries: Int = 10, interval: FiniteDuration = 2 seconds)(implicit runner: TestRunner, client: HttpClient, logger: Logger): Boolean = {
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

  private[test] def scrapeMetrics(filterID: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger): MetricsCollection = {
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

    metrics
  }

  private[test] def cleanUp(implicit runner: TestRunner, client: HttpClient, logger: Logger): Unit = {
    import runner._

    applyBehavior(new DeleteAllBlueprints())
    applyBehavior(new DeleteAllConfigurations())
    applyBehavior(new DeleteAllPipelines())

  }

}

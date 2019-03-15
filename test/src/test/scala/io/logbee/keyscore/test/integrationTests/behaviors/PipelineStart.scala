package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import org.slf4j.Logger

class PipelineStart(pipelineObject: PipelineBlueprint, pipelineID: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {
  override def apply(): Unit = {
    logger.debug(s"START Pipeline <${pipelineObject.ref.uuid}>")

    runner.http(action => action.client(client)
      .send()
      .put(s"/pipeline/blueprint")
      .contentType("application/json")
      .payload(pipelineID)
    )
  }
}

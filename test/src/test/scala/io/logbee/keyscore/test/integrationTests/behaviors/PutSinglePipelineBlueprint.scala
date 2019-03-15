package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import org.slf4j.Logger
import org.springframework.http.HttpStatus

class PutSinglePipelineBlueprint(pipelineObject: PipelineBlueprint, pipelineJSON: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {
  override def apply(): Unit = {
    logger.debug(s"PUT PipelineBlueprint for <${pipelineObject.ref.uuid}>")

    runner.http(action => action.client(client)
      .send()
      .put(s"/resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
      .contentType("application/json")
      .payload(pipelineJSON)
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }
}

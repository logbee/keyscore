package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import org.slf4j.Logger
import org.springframework.http.HttpStatus

class DeleteSinglePipelineBlueprint(blueprintID: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {
  override def apply(): Unit = {
    logger.debug(s"DELETE PipelineBlueprint for <${blueprintID}>")

    runner.http(action => action.client(client)
      .send()
      .delete(s"resources/blueprint/pipeline/${blueprintID}")
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK))
  }
}

package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.blueprint.SealedBlueprint
import io.logbee.keyscore.model.blueprint.ToBase.sealedToBase
import org.slf4j.Logger
import org.springframework.http.HttpStatus

class PutSingleBlueprint(blueprintObject: SealedBlueprint, pipelineJSON: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {

  override def apply(): Unit = {
    logger.debug(s"PUT Blueprint for <${blueprintObject.blueprintRef.uuid}>")

    runner.http(action => action.client(client)
      .send()
      .put(s"/resources/blueprint/${blueprintObject.blueprintRef.uuid}")
      .contentType("application/json")
      .payload(pipelineJSON)
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
    )
  }
}

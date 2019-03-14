package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import org.slf4j.Logger

class DeleteAllBlueprints()(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {
  override def apply(): Unit = {
    logger.debug(s"DELETE ALL blueprints")

    runner.http(action => action.client(client)
      .send()
      .delete(s"/resources/blueprint/pipeline/*")
    )

    runner.http(action => action.client(client)
      .send()
      .delete(s"/resources/blueprint/*")
    )
  }
}

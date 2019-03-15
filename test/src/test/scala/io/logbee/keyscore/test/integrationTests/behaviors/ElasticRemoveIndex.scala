package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import org.slf4j.Logger
import org.springframework.http.HttpStatus

class ElasticRemoveIndex(index: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {
  override def apply(): Unit = {
    logger.debug(s"REMOVE Elastic Index for ${index}")

    runner.http(action => action.client(client)
      .send()
      .delete("/" + index))

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
    )
  }
}

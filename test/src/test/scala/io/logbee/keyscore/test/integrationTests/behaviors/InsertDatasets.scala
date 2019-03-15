package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import org.slf4j.Logger
import org.springframework.http.HttpStatus

class InsertDatasets(filterID: String, datasets: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {
  override def apply(): Unit = {
    logger.debug(s"INSERT Dataset for <${filterID}> with ${datasets}")

    runner.http(action => action.client(client)
      .send()
      .put(s"/filter/${filterID}/insert")
      .contentType("application/json")
      .payload(datasets)
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
  }
}

package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.configuration.Configuration
import org.slf4j.Logger
import org.springframework.http.HttpStatus

class PutSingleConfiguration(configurationObject: Configuration, configurationJSON: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {
  override def apply(): Unit = {
    logger.debug(s"PUT Configuration for <${configurationObject.ref.uuid}>")

    runner.http(action => action.client(client)
      .send()
      .put(s"/resources/configuration/${configurationObject.ref.uuid}")
      .contentType("application/json")
      .payload(configurationJSON)
    )

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.OK)
    )
  }
}

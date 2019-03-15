package io.logbee.keyscore.test.integrationTests.behaviors

import com.consol.citrus.dsl.runner.{AbstractTestBehavior, TestRunner}
import com.consol.citrus.http.client.HttpClient
import org.slf4j.Logger
import org.springframework.http.HttpStatus

class FilterDrain(filterId: String, toggle: String)(implicit runner: TestRunner, client: HttpClient, logger: Logger) extends AbstractTestBehavior {
  override def apply(): Unit = {
    logger.debug(s"DRAIN ($toggle) Filter for <${filterId}>")

    runner.http(action => action.client(client)
      .send()
      .post(s"/filter/${filterId}/drain?value=" + toggle))

    runner.http(action => action.client(client)
      .receive()
      .response(HttpStatus.ACCEPTED)
    )
  }
}

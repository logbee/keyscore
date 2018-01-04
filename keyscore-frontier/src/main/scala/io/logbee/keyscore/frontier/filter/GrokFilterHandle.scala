package io.logbee.keyscore.frontier.filter

import scala.concurrent.Future

trait GrokFilterHandle {

  def configure(config: GrokFilterConfiguration): Future[Boolean]
}

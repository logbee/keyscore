package io.logbee.keyscore.frontier.filters

import scala.concurrent.Future

trait GrokFilterHandle {

  def configure(config: GrokFilterConfiguration): Future[Boolean]
}

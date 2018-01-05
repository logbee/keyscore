package io.logbee.keyscore.frontier.filters

import scala.concurrent.Future

trait GrokFilterHandle extends FilterHandle {

  def configure(config: GrokFilterConfiguration): Future[Boolean]
}

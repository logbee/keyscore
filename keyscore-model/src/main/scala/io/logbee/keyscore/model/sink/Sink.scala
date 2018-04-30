package io.logbee.keyscore.model.sink

import io.logbee.keyscore.model.Condition
import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.concurrent.Future

trait Sink {

  def changeCondition(condition: Condition): Future[Unit]

  def changeFunction(function: SinkFunction): Future[Unit]

  def configureCondition(configuration: FilterConfiguration): Future[Unit]

  def configureFunction(configuration: FilterConfiguration): Future[Unit]
}

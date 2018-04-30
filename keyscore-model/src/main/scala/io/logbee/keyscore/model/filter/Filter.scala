package io.logbee.keyscore.model.filter

import io.logbee.keyscore.model.Condition

import scala.concurrent.Future

trait Filter {

  def changeCondition(condition: Condition): Future[Boolean]

  def changeFunction(function: FilterFunction): Future[Boolean]

  def configureCondition(configuration: FilterConfiguration): Future[Boolean]

  def configureFunction(configuration: FilterConfiguration): Future[Boolean]
}

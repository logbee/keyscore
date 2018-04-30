package io.logbee.keyscore.model.filter

import scala.concurrent.Future

trait Filter {

  def changeCondition(trigger: FilterCondition): Future[Boolean]

  def changeFunction(function: FilterFunction): Future[Boolean]

  def configureCondition(configuration: FilterConfiguration): Future[Boolean]

  def configureFunction(configuration: FilterConfiguration): Future[Boolean]
}

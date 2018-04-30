package io.logbee.keyscore.model.filter

import io.logbee.keyscore.model.Condition

import scala.concurrent.Future

trait Filter {

  def changeCondition(condition: Condition): Future[Unit]

  def changeFunction(function: FilterFunction): Future[Unit]

  def configureCondition(configuration: FilterConfiguration): Future[Unit]

  def configureFunction(configuration: FilterConfiguration): Future[Unit]
}

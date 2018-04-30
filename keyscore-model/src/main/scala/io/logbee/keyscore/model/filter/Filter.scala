package io.logbee.keyscore.model.filter

import io.logbee.keyscore.model.Dataset

import scala.concurrent.Future

trait Filter {

  def changeCondition(trigger: FilterCondition): Future[Boolean]

  def changeFunction(function: FilterFunction): Future[Boolean]

  def configureCondition(configuration: FilterConfiguration): Future[Boolean]

  def configureFunction(configuration: FilterConfiguration): Future[Boolean]
}

trait FilterConditionResult { }
case class Accept(dataset: Dataset) extends FilterConditionResult
case class Reject(dataset: Dataset) extends FilterConditionResult
case class Drop(dataset: Dataset) extends FilterConditionResult

trait FilterCondition {

  def configure(configuration: FilterConfiguration): Boolean

  def apply(dataset: Dataset): FilterConditionResult
}

trait FilterFunction {

  def configure(configuration: FilterConfiguration): Boolean

  def apply(dataset: Dataset): Dataset
}
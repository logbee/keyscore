package io.logbee.keyscore.model

trait ConditionResult { }

case class Accept(dataset: Dataset) extends ConditionResult
case class Reject(dataset: Dataset) extends ConditionResult
case class Drop(dataset: Dataset) extends ConditionResult
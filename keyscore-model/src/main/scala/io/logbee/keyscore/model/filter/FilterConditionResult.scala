package io.logbee.keyscore.model.filter

import io.logbee.keyscore.model.Dataset

trait FilterConditionResult { }

case class Accept(dataset: Dataset) extends FilterConditionResult
case class Reject(dataset: Dataset) extends FilterConditionResult
case class Drop(dataset: Dataset) extends FilterConditionResult
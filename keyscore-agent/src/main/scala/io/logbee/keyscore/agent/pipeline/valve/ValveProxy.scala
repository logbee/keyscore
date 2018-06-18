package io.logbee.keyscore.agent.pipeline.valve

import io.logbee.keyscore.model.Dataset

import scala.concurrent.Future

trait ValveProxy {

  def state(): Future[ValveState]

  def open(): Future[ValveState]

  def close(): Future[ValveState]

  def drain(): Future[ValveState]

  def extract(amount: Int = 1): Future[List[Dataset]]

  def insert(dataset: List[Dataset]): Future[ValveState]

  def clearBuffer(): Future[ValveState]
}

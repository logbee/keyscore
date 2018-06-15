package io.logbee.keyscore.agent.pipeline.valve

import io.logbee.keyscore.model.Dataset

import scala.concurrent.Future

trait ValveProxy {

  def state(): Future[ValveState]

  def pause(pause: Boolean): Future[ValveState]

  def drain(drain: Boolean): Future[ValveState]

  def extract(amount: Int = 1): Future[List[Dataset]]

  def insert(dataset: Dataset*): Future[ValveState]

  def clearBuffer(): Future[ValveState]
}

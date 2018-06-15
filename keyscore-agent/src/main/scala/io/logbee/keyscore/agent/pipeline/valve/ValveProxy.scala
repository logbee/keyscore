package io.logbee.keyscore.agent.pipeline.valve

import io.logbee.keyscore.model.Dataset

import scala.concurrent.Future

trait ValveProxy {
  def state(): Future[ValveState]

  def pause(doPause: Boolean): Future[ValveState]

  def extract(n: Int = 1): Future[List[Dataset]]

  def insert(dataset: Dataset*): Future[ValveState]

  def allowDrain(drainAllowed: Boolean): Future[ValveState]

  def clearBuffer(): Future[ValveState]
}

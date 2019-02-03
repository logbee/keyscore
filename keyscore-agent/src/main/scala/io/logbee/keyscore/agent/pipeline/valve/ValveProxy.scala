package io.logbee.keyscore.agent.pipeline.valve

import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.metrics.MetricsCollection

import scala.concurrent.Future

trait ValveProxy {

  def state(): Future[ValveState]

  def open(): Future[ValveState]

  def close(): Future[ValveState]

  def drain(): Future[ValveState]

  def extract(amount: Int = 1): Future[List[Dataset]]

  def insert(dataset: List[Dataset]): Future[ValveState]

  def scrape(): Future[MetricsCollection]

  def clear(): Future[ValveState]
}

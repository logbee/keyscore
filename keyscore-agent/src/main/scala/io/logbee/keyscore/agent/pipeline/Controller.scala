package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterProxy, FilterState}
import io.logbee.keyscore.model.sink.SinkProxy
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.{ExecutionContext, Future}

object Controller {

  def sourceController(sourceProxy: SourceProxy, valveProxy: ValveProxy): Controller = {
    new SourceController(sourceProxy, valveProxy)
  }

  def filterController(inValveProxy: ValveProxy, filterProxy: FilterProxy, outValveProxy: ValveProxy)(implicit executionContext: ExecutionContext): Controller = {
    new FilterController(inValveProxy, filterProxy, outValveProxy)
  }

  def sinkController(valveProxy: ValveProxy, sinkProxy: SinkProxy): Controller = {
    new SinkController(valveProxy, sinkProxy)
  }
}

abstract class Controller {

  val id: UUID

  def configure(configuration: FilterConfiguration): Future[Unit]

  def pause(doClose: Boolean): Future[FilterState]

  def drain(drain: Boolean): Future[FilterState]

  def insert(dataset: List[Dataset]): Future[FilterState]

  def extract(amount: Int = 1): Future[List[Dataset]]
}

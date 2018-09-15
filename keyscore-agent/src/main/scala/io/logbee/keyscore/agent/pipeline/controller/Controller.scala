package io.logbee.keyscore.agent.pipeline.controller

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.WhichValve
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline._

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

  def branchController(inValve: ValveProxy, branch: BranchProxy, leftValve: ValveProxy, rightValve: ValveProxy): Controller = {
    new BranchController(inValve, branch, leftValve, rightValve)
  }

  def mergeController(leftValve: ValveProxy, rightValve: ValveProxy, merge: MergeProxy, outValve: ValveProxy): Controller = {
    new MergeController(leftValve, rightValve, merge, outValve)
  }
}

abstract class Controller {

  val id: UUID

  def configure(configuration: Configuration): Future[FilterState]

  def pause(doClose: Boolean): Future[FilterState]

  def drain(drain: Boolean): Future[FilterState]

  def insert(dataset: List[Dataset], where: WhichValve): Future[FilterState]

  def extract(amount: Int = 1, where: WhichValve): Future[List[Dataset]]

  def state(): Future[FilterState]

  def clear(): Future[FilterState]
}

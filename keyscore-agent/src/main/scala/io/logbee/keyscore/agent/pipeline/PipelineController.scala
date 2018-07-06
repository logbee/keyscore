package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.{Dataset, PipelineConfiguration}

import scala.concurrent.Future

class PipelineController(val pipeline: Pipeline, val controllers: List[Controller]) {

  private val controllerMap = controllers.map(controller => controller.id -> controller).toMap

  def configuration: PipelineConfiguration = pipeline.configuration

  def id: UUID = configuration.id

  def configure(id: UUID, configuration: FilterConfiguration): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.configure(configuration))
  }

  def close(id: UUID, doClose: Boolean): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.pause(doClose))
  }

  def drain(id: UUID, doDrain: Boolean): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.drain(doDrain))
  }

  def insert(id: UUID, dataset: List[Dataset]): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.insert(dataset))
  }

  def extract(id: UUID, amount: Int = 1): Option[Future[List[Dataset]]] = {
    controllerMap.get(id).map(_.extract(amount))
  }

  def state(id: UUID): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.state())
  }
}

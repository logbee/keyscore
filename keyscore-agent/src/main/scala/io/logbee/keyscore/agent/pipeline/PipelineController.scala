package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.{Dataset, PipelineConfiguration}

import scala.concurrent.Future

class PipelineController(val pipeline: Pipeline, val controllers: List[Controller]) {

  private val lookup = controllers.map(controller => controller.id -> controller).toMap

  def configuration: PipelineConfiguration = pipeline.configuration

  def id: UUID = configuration.id

  def configure(id: UUID, configuration: FilterConfiguration): Future[Unit] = {
    lookup(id).configure(configuration)
  }

  def pause(id: UUID, doPause: Boolean): Future[FilterState] = {
    lookup(id).pause(doPause)
  }

  def drain(id: UUID, doDrain: Boolean): Future[FilterState] = {
    lookup(id).drain(doDrain)
  }

  def insert(id: UUID, dataset: Dataset*): Future[FilterState] = {
    lookup(id).insert(dataset: _*)
  }

  def extract(id: UUID, amount: Int = 1): Future[List[Dataset]] = {
    lookup(id).extract(amount)
  }
}

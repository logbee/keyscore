package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.controller.Controller
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidFromString
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.model.pipeline.FilterState

import scala.concurrent.{ExecutionContext, Future}

/**
  * The '''PipelineController''' holds the `Pipeline` and a list of `Controllers` for each ~Filter~. <br><br>
  * He forwards all requests to the matching `Controllers`.
  *
  * @param pipeline    The Pipeline object.
  * @param controllers The list of Controllers.
  */
class PipelineController(val pipeline: Pipeline, val controllers: List[Controller])(implicit ex: ExecutionContext) {

  private val controllerMap = controllers.map(controller => controller.id -> controller).toMap

  def pipelineBlueprint: PipelineBlueprint = pipeline.pipelineBlueprint

  def id: UUID = pipelineBlueprint.ref.uuid

  def configure(id: UUID, configuration: Configuration): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.configure(configuration))
  }

  def close(id: UUID, doClose: Boolean): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.pause(doClose))
  }

  def drain(id: UUID, doDrain: Boolean): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.drain(doDrain))
  }

  def insert(id: UUID, dataset: List[Dataset], where: WhichValve): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.insert(dataset, Before))
  }

  def extract(id: UUID, amount: Int = 1, where: WhichValve): Option[Future[List[Dataset]]] = {
    controllerMap.get(id).map(_.extract(amount, where))
  }

  def state(id: UUID): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.state())
  }

  def clear(id: UUID): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.clear())
  }

  def scrape(id: UUID): Option[Future[MetricsCollection]] = {
    controllerMap.get(id).map(_.scrape())
  }

  def scrapePipeline(): Future[Map[UUID, MetricsCollection]] = {
    val metrics = controllerMap.map { case (id, controller) => (id, controller.scrape()) }
    transformMap(metrics)
  }

  private def transformMap[MetricsCollections](map: Map[UUID, Future[MetricsCollection]]): Future[Map[UUID, MetricsCollection]] = {
    val seq: Seq[Future[(UUID, MetricsCollection)]] = map.toSeq.map { case (k, v) =>
      v.map(mc => k -> mc)
    }

    Future.foldLeft(seq.toList)(Map.empty[UUID, MetricsCollection])(_ + _)
  }

}

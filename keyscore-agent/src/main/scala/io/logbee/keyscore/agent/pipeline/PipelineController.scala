package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.controller.Controller
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidFromString
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.FilterState

import scala.concurrent.Future

class PipelineController(val pipeline: Pipeline, val controllers: List[Controller]) {

  private val controllerMap = controllers.map(controller => controller.id -> controller).toMap

  def pipelineBlueprint: PipelineBlueprint = pipeline.pipelineBlueprint

  def id: UUID = pipelineBlueprint.ref.uuid

  def configure(id: UUID, configuration: Configuration): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.configure(configuration))
  }

  def close(id: UUID, doClose: Boolean): Option[Future[FilterState]] = {
    println(s"closing valve <$id>")
    controllerMap.foreach( kv => {
      println(s"Key: ${kv._1} -> Value: ${kv._2}")
    })
    controllerMap.get(id).map(_.pause(doClose))
  }

  def drain(id: UUID, doDrain: Boolean): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.drain(doDrain))
  }

  def insert(id: UUID, dataset: List[Dataset], where: WhichValve): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.insert(dataset, Before))
  }

  def extract(id: UUID, amount: Int = 1, where:WhichValve): Option[Future[List[Dataset]]] = {
    controllerMap.get(id).map(_.extract(amount, where))
  }

  def state(id: UUID): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.state())
  }

  def clear(id: UUID): Option[Future[FilterState]] = {
    controllerMap.get(id).map(_.clear())
  }
}

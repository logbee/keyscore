package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.stage.ValveProxy
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterProxy}
import io.logbee.keyscore.model.sink.SinkProxy
import io.logbee.keyscore.model.source.SourceProxy
import io.logbee.keyscore.model.{Dataset, PipelineConfiguration}

import scala.concurrent.Future

class PipelineController(val pipeline: Pipeline, val controllers: List[Controller]) {

  private val lookup = controllers.map(controller => controller.id -> controller).toMap

  def configuration: PipelineConfiguration = pipeline.configuration

  def id: UUID = configuration.id

  def configure(id: UUID, configuration: FilterConfiguration): Future[Unit] = {
    lookup(id).configure(configuration)
  }

  def pause(id: UUID): Future[Unit] = {
    lookup(id).pause()
  }

  def unpause(id: UUID): Future[Unit] = {
    lookup(id).unpause()
  }

  def drain(id: UUID, doDrain: Boolean): Future[Boolean] = {
    lookup(id).drain(doDrain)
  }

  def insert(id: UUID, dataset: Dataset*): Future[Unit] = {
    lookup(id).insert(dataset:_*)
  }

  def extract(id: UUID, amount: Int = 1): Future[List[Dataset]] = {
    lookup(id).extract(amount)
  }
}

object Controller {

  def sourceController(sourceProxy: SourceProxy, valveProxy: ValveProxy): Controller = {
    new SourceController(sourceProxy, valveProxy)
  }

  def filterController(inValveProxy: ValveProxy, filterProxy: FilterProxy, outValveProxy: ValveProxy): Controller = {
    new FilterController(inValveProxy, filterProxy, outValveProxy)
  }

  def sinkController(valveProxy: ValveProxy, sinkProxy: SinkProxy): Controller = {
    new SinkController(valveProxy, sinkProxy)
  }
}

abstract class Controller {

  val id: UUID

  def configure(configuration: FilterConfiguration): Future[Unit]

  def pause(): Future[Unit]

  def unpause(): Future[Unit]

  def drain(drain: Boolean): Future[Boolean]

  def insert(dataset: Dataset*): Future[Unit]

  def extract(amount: Int = 1): Future[List[Dataset]]
}

private class SourceController(val source: SourceProxy, val valve: ValveProxy) extends Controller {

  override val id: UUID = ???

  override def configure(configuration: FilterConfiguration): Future[Unit] = ???

  override def pause(): Future[Unit] = ???

  override def unpause(): Future[Unit] = ???

  override def drain(drain: Boolean): Future[Boolean] = ???

  override def insert(dataset: Dataset*): Future[Unit] = ???

  override def extract(n: Int): Future[List[Dataset]] = ???

}

private class SinkController(val valve: ValveProxy, val sink: SinkProxy) extends Controller {

  override val id: UUID = ???

  override def configure(configuration: FilterConfiguration): Future[Unit] = ???

  override def pause(): Future[Unit] = ???

  override def unpause(): Future[Unit] = ???

  override def drain(drain: Boolean): Future[Boolean] = ???

  override def insert(dataset: Dataset*): Future[Unit] = ???

  override def extract(n: Int): Future[List[Dataset]] = ???
}

private class FilterController(val inValve: ValveProxy, val filter: FilterProxy, val outValve: ValveProxy) extends Controller {

  override val id: UUID = ???

  override def configure(configuration: FilterConfiguration): Future[Unit] = ???

  override def pause(): Future[Unit] = ???

  override def unpause(): Future[Unit] = ???

  override def drain(drain: Boolean): Future[Boolean] = ???

  override def insert(dataset: Dataset*): Future[Unit] = ???

  override def extract(n: Int): Future[List[Dataset]] = ???
}
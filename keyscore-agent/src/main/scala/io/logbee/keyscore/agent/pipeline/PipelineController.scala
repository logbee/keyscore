package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.stage.{ValveProxy, ValveState}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterProxy, FilterState}
import io.logbee.keyscore.model.sink.SinkProxy
import io.logbee.keyscore.model.source.SourceProxy
import io.logbee.keyscore.model.{Dataset, PipelineConfiguration}

import scala.concurrent.{ExecutionContext, Future}

class PipelineController(val pipeline: Pipeline, val controllers: List[Controller]) {

  private val lookup = controllers.map(controller => controller.id -> controller).toMap

  def configuration: PipelineConfiguration = pipeline.configuration

  def id: UUID = configuration.id

  def configure(id: UUID, configuration: FilterConfiguration): Future[Unit] = {
    lookup(id).configure(configuration)
  }

  def pause(id: UUID): Future[FilterState] = {
    lookup(id).pause()
  }

  def unpause(id: UUID): Future[FilterState] = {
    lookup(id).unpause()
  }

  def drain(id: UUID, doDrain: Boolean): Future[FilterState] = {
    lookup(id).drain(doDrain)
  }

  def insert(id: UUID, dataset: Dataset*): Future[FilterState] = {
    lookup(id).insert(dataset:_*)
  }

  def extract(id: UUID, amount: Int = 1): Future[List[Dataset]] = {
    lookup(id).extractLiveData(amount)
  }
}

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

  def pause(): Future[FilterState]

  def unpause(): Future[FilterState]

  def drain(drain: Boolean): Future[FilterState]

  def insert(dataset: Dataset*): Future[FilterState]

  def extractLiveData(amount: Int = 1): Future[List[Dataset]]

  def extractInsertedData(amount: Int = 1): Future[List[Dataset]]
}

private class SourceController(val source: SourceProxy, val valve: ValveProxy) extends Controller {

  override val id: UUID = source.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = source.configure(configuration)

  override def pause(): Future[FilterState] = ???

  override def unpause(): Future[FilterState] = ???

  override def drain(drain: Boolean): Future[FilterState] = ???

  override def insert(dataset: Dataset*): Future[FilterState] = ???

  override def extractLiveData(n: Int): Future[List[Dataset]] = valve.extractDatasets(n)

  override def extractInsertedData(n: Int): Future[List[Dataset]] = valve.extractDatasets(n)
}

private class SinkController(val valve: ValveProxy, val sink: SinkProxy) extends Controller {

  override val id: UUID = sink.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = sink.configure(configuration)

  override def pause(): Future[FilterState] = ???

  override def unpause(): Future[FilterState] = ???

  override def drain(drain: Boolean): Future[FilterState] = ???

  override def insert(dataset: Dataset*): Future[FilterState] = ???

  override def extractLiveData(n: Int): Future[List[Dataset]] = valve.extractDatasets(n)

  override def extractInsertedData(n: Int): Future[List[Dataset]] = valve.extractDatasets(n)
}

private class FilterController(val inValve: ValveProxy, val filter: FilterProxy, val outValve: ValveProxy) (implicit val executionContext: ExecutionContext) extends Controller {

  override val id: UUID = filter.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = filter.configure(configuration)

  override def pause(): Future[FilterState] = {
    for {
      _ <- inValve.pause()
      _ <- outValve.pause()
      filterState <- filter.state()
    } yield filterState
  }

  override def unpause(): Future[FilterState] = {
    for {
      _ <- inValve.unpause()
      _ <- outValve.unpause()
      filterState <- filter.state()
    } yield filterState
  }

  override def drain(drain: Boolean): Future[FilterState] = {
    for {
      _ <- outValve.allowDrain(drain)
      filterState <- filter.state()
    } yield filterState
  }

  override def insert(dataset: Dataset*): Future[FilterState] = {

    for {
      _ <- inValve.pause()
      _ <- outValve.pause()
      _ <- inValve.insert(dataset:_*)
      filterState <- filter.state()
    } yield filterState
  }

  override def extractInsertedData(n: Int): Future[List[Dataset]] = outValve.extractDatasets(n)

  override def extractLiveData(n: Int): Future[List[Dataset]] = outValve.extractDatasets(n)
}
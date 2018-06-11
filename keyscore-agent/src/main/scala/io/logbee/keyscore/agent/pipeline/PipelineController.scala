package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.stage.{ValveProxy, ValveState}
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

  def pause(id: UUID): Future[ValveState] = {
    lookup(id).pause()
  }

  def unpause(id: UUID): Future[ValveState] = {
    lookup(id).unpause()
  }

  def drain(id: UUID, doDrain: Boolean): Future[ValveState] = {
    lookup(id).drain(doDrain)
  }

  def insert(id: UUID, dataset: Dataset*): Future[ValveState] = {
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

  def pause(): Future[ValveState]

  def unpause(): Future[ValveState]

  def drain(drain: Boolean): Future[ValveState]

  def insert(dataset: Dataset*): Future[ValveState]

  def extractLiveData(amount: Int = 1): Future[List[Dataset]]

  def extractInsertedData(amount: Int = 1): Future[List[Dataset]]
}

private class SourceController(val source: SourceProxy, val valve: ValveProxy) extends Controller {

  override val id: UUID = source.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = source.configure(configuration)

  override def pause(): Future[ValveState] = valve.pause()

  override def unpause(): Future[ValveState] = valve.unpause()

  override def drain(drain: Boolean): Future[ValveState] = if (drain) {
    valve.allowDrain()
  } else {
    valve.denyDrain()
  }

  override def insert(dataset: Dataset*): Future[ValveState] = valve.insert(dataset:_*)

  override def extractLiveData(n: Int): Future[List[Dataset]] = valve.extractLiveDatasets(n)

  override def extractInsertedData(n: Int): Future[List[Dataset]] = valve.extractInsertedDatasets(n)
}

private class SinkController(val valve: ValveProxy, val sink: SinkProxy) extends Controller {

  override val id: UUID = sink.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = sink.configure(configuration)

  override def pause(): Future[ValveState] = valve.pause()

  override def unpause(): Future[ValveState] = valve.unpause()

  override def drain(drain: Boolean): Future[ValveState] = if (drain) {
    valve.allowDrain()
  } else {
    valve.denyDrain()
  }

  override def insert(dataset: Dataset*): Future[ValveState] = valve.insert(dataset:_*)

  override def extractLiveData(n: Int): Future[List[Dataset]] = valve.extractLiveDatasets(n)

  override def extractInsertedData(n: Int): Future[List[Dataset]] = valve.extractInsertedDatasets(n)
}

private class FilterController(val inValve: ValveProxy, val filter: FilterProxy, val outValve: ValveProxy) extends Controller {

  override val id: UUID = filter.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = filter.configure(configuration)

  override def pause(): Future[ValveState] = {
    inValve.pause()
    outValve.pause()
  }

  override def unpause(): Future[ValveState] = {
    inValve.unpause()
    outValve.unpause()
  }

  override def drain(drain: Boolean): Future[ValveState] = if (drain) {
    outValve.allowDrain()
  } else {
    outValve.denyDrain()
  }

  override def insert(dataset: Dataset*): Future[ValveState] = {
    println("Started insert Method")
    inValve.pause()
    println("InValve paused")
    outValve.pause()
    println("OutValve paused")
    outValve.allowPull()
    println("OutValve allowed pull")
    println("InValve insert" +  dataset)
    inValve.insert(dataset:_*)
  }
  override def extractInsertedData(n: Int): Future[List[Dataset]] = outValve.extractInsertedDatasets(n)

  override def extractLiveData(n: Int): Future[List[Dataset]] = outValve.extractLiveDatasets(n)
}
package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.{Dataset, WhichValve}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.sink.SinkProxy

import scala.concurrent.Future

private class SinkController(val valve: ValveProxy, val sink: SinkProxy) extends Controller {

  override val id: UUID = sink.id

  override def configure(configuration: FilterConfiguration): Future[FilterState] = sink.configure(configuration)

  override def pause(doClose: Boolean): Future[FilterState] = ???

  override def drain(drain: Boolean): Future[FilterState] = ???

  override def insert(dataset: List[Dataset], whichValve: WhichValve): Future[FilterState] = ???

  override def extract(n: Int, whichValve: WhichValve): Future[List[Dataset]] = valve.extract(n)

  override def state(): Future[FilterState] = ???

  override def clear(): Future[FilterState] = ???
}

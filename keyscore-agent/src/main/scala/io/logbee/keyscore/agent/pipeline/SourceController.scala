package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.{Dataset, WhichValve}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.Future

private class SourceController(val source: SourceProxy, val valve: ValveProxy) extends Controller {

  override val id: UUID = source.id

  override def configure(configuration: FilterConfiguration): Future[FilterState] = source.configure(configuration)

  override def pause(doClose: Boolean): Future[FilterState] = ???

  override def drain(drain: Boolean): Future[FilterState] = ???

  override def insert(dataset: List[Dataset], whichValve: WhichValve): Future[FilterState] = ???

  override def extract(n: Int, whichValve: WhichValve): Future[List[Dataset]] = valve.extract(n)

  override def state(): Future[FilterState] = ???

  override def clear(): Future[FilterState] = ???
}

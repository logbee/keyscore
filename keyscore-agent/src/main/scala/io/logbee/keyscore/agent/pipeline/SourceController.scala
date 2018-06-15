package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.Future

private class SourceController(val source: SourceProxy, val valve: ValveProxy) extends Controller {

  override val id: UUID = source.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = source.configure(configuration)

  override def pause(doPause: Boolean): Future[FilterState] = ???

  override def drain(drain: Boolean): Future[FilterState] = ???

  override def insert(dataset: Dataset*): Future[FilterState] = ???

  override def extract(n: Int): Future[List[Dataset]] = valve.extract(n)
}

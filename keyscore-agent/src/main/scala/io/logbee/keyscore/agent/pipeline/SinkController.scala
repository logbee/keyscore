package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.sink.SinkProxy

import scala.concurrent.Future

private class SinkController(val valve: ValveProxy, val sink: SinkProxy) extends Controller {

  override val id: UUID = sink.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = sink.configure(configuration)

  override def pause(doClose: Boolean): Future[FilterState] = ???

  override def drain(drain: Boolean): Future[FilterState] = ???

  override def insert(dataset: List[Dataset]): Future[FilterState] = ???

  override def extract(n: Int): Future[List[Dataset]] = valve.extract(n)
}

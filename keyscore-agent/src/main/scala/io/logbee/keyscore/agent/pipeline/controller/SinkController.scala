package io.logbee.keyscore.agent.pipeline.controller

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.WhichValve
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.{FilterState, SinkProxy}

import scala.concurrent.Future

private class SinkController(val valve: ValveProxy, val sink: SinkProxy) extends Controller {

  override val id: UUID = sink.id

  override def configure(configuration: Configuration): Future[FilterState] = sink.configure(configuration)

  override def pause(doClose: Boolean): Future[FilterState] = ???

  override def drain(drain: Boolean): Future[FilterState] = ???

  override def insert(dataset: List[Dataset], whichValve: WhichValve): Future[FilterState] = ???

  override def extract(n: Int, whichValve: WhichValve): Future[List[Dataset]] = valve.extract(n)

  override def state(): Future[FilterState] = ???

  override def clear(): Future[FilterState] = ???
}

package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.Future

private class SourceController(val source: SourceProxy, val valve: ValveProxy) extends Controller {

  override val id: UUID = source.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = {
   for {
       _ <- source.configure(configuration)
      sourceState <- source.state()
   } yield sourceState
  }

  override def pause(doPause: Boolean): Future[FilterState] = {
    for {
      _ <- if (doPause) valve.close() else valve.open()
      sourceState <- source.state()
    } yield sourceState
  }
  override def drain(drain: Boolean): Future[FilterState] = {
    for {
      _ <- if (drain) valve.drain() else valve.open()
      filterState <- source.state()
    } yield filterState
  }

  override def insert(dataset: List[Dataset]): Future[FilterState] = ???

  override def extract(n: Int): Future[List[Dataset]] = valve.extract(n)
}

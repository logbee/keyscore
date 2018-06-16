package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterProxy, FilterState}

import scala.concurrent.{ExecutionContext, Future}

private class FilterController(val inValve: ValveProxy, val filter: FilterProxy, val outValve: ValveProxy)(implicit val executionContext: ExecutionContext) extends Controller {

  override val id: UUID = filter.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = filter.configure(configuration)

  override def pause(doPause: Boolean): Future[FilterState] = {
    for {
      _ <- if (doPause) inValve.close() else inValve.open()
      filterState <- filter.state()
    } yield filterState
  }

  override def drain(drain: Boolean): Future[FilterState] = {
    for {
      _ <- if (drain) outValve.drain() else outValve.open()
      filterState <- filter.state()
    } yield filterState
  }

  override def insert(dataset: Dataset*): Future[FilterState] = {
    for {
      _ <- inValve.close()
      _ <- outValve.drain()
      _ <- inValve.insert(dataset: _*)
      filterState <- filter.state()
    } yield filterState
  }

  override def extract(n: Int): Future[List[Dataset]] = {
    for {
      result <- outValve.extract(n)
    } yield result
  }

}

package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterProxy, FilterState}

import scala.concurrent.{ExecutionContext, Future}

private class FilterController(val inValve: ValveProxy, val filter: FilterProxy, val outValve: ValveProxy)(implicit val executionContext: ExecutionContext) extends Controller {

  override val id: UUID = filter.id

  override def configure(configuration: FilterConfiguration): Future[Unit] = {
    for {
      _ <- filter.configure(configuration)
      filterState <- filter.state()
    } yield filterState
  }

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

  override def insert(dataset: List[Dataset]): Future[FilterState] = {
    for {
      _ <- inValve.insert(dataset)
      filterState <- filter.state()
    } yield filterState
  }

  override def extract(n: Int): Future[List[Dataset]] = {
    for {
      result <- outValve.extract(n)
    } yield result
  }

}

package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.{ValvePosition, ValveProxy, ValveState}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{After, Dataset, WhichValve}

import scala.concurrent.{ExecutionContext, Future}

private class FilterController(val inValve: ValveProxy, val filter: FilterProxy, val outValve: ValveProxy)(implicit val executionContext: ExecutionContext) extends Controller {

  override val id: UUID = filter.id

  override def configure(configuration: FilterConfiguration): Future[FilterState] = {
    for {
    inValveState <- inValve.state()
    outValveState <- outValve.state()
    _ <- filter.configure(configuration)
    filterState <- filter.state()
    } yield computeFilterState(inValveState, outValveState, filterState)
  }

  override def pause(doPause: Boolean): Future[FilterState] = {
    for {
    inValveState <- inValve.state()
    outValveState <- outValve.state()
    _ <- if (doPause) inValve.close() else inValve.open()
    filterState <- filter.state()
    } yield computeFilterState(inValveState, outValveState, filterState)
  }

  override def drain(drain: Boolean): Future[FilterState] = {
    for {
    inValveState <- inValve.state()
    outValveState <- outValve.state()
    _ <- if (drain) outValve.drain() else outValve.open()
    filterState <- filter.state()
    } yield computeFilterState(inValveState, outValveState, filterState)
  }

  override def insert(dataset: List[Dataset], whichValve: WhichValve): Future[FilterState] = {
    for {
    inValveState <- inValve.state()
    outValveState <- outValve.state()
    _ <- if(whichValve == After) outValve.insert(dataset) else inValve.insert(dataset)
    filterState <- filter.state()
    } yield computeFilterState(inValveState, outValveState, filterState)
  }

  override def extract(n: Int, whichValve: WhichValve): Future[List[Dataset]] = {
    for {
    result <- if(whichValve == After) outValve.extract(n) else inValve.extract(n)
    } yield result
  }

  override def state(): Future[FilterState] = {
    for {
    inValveState <- inValve.state()
    outValveState <- outValve.state()
    filterState <- filter.state()
    } yield  computeFilterState(inValveState, outValveState, filterState)
  }

  override def clear(): Future[FilterState] = {
    for {
      inValveState <- inValve.state()
      outValveState <- outValve.state()
      filterState <- filter.state()
    } yield  computeFilterState(inValveState, outValveState, filterState)
  }

  private def determineFilterStatus(in: ValveState, out: ValveState): FilterStatus = {
    val status = (in.position, out.position) match {
      case (ValvePosition.Closed, ValvePosition.Open) => Paused
      case (ValvePosition.Closed, ValvePosition.Drain) => Ready
      case (ValvePosition.Open, ValvePosition.Open) => Running
      case (ValvePosition.Open, ValvePosition.Drain) => Drained
      case _ => Unknown
    }
    status
  }

  private def computeFilterState(inValveState: ValveState, outValveState: ValveState, filterState: FilterState) = {
    FilterState(filterState.id, filterState.health, outValveState.throughputTime, outValveState.totalThroughputTime, determineFilterStatus(inValveState, outValveState))
  }
}

package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.{ValvePosition, ValveProxy, ValveState}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter._

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

  override def insert(dataset: List[Dataset]): Future[FilterState] = {
    for {
    inValveState <- inValve.state()
    outValveState <- outValve.state()
    _ <- inValve.insert(dataset)
    filterState <- filter.state()
    } yield computeFilterState(inValveState, outValveState, filterState)
  }

  override def extract(n: Int): Future[List[Dataset]] = {
    for {
    result <- outValve.extract(n)
    } yield result
  }

  override def state(): Future[FilterState] = {
    for {
    inValveState <- inValve.state()
    outValveState <- outValve.state()
    filterState <- filter.state()
    } yield  computeFilterState(inValveState, outValveState, filterState)
  }

  private def determineFilterStatus(in: ValveState, out: ValveState): FilterStatus = {
    val status = (in.position, out.position) match {
      case (ValvePosition.Closed, _) => Paused
      case (ValvePosition.Open, ValvePosition.Open) => Running
      case (_, ValvePosition.Drain) => Drained
      case _ => Unknown
    }
    println(s"Status: $status ${in.position} ${out.position}")
    status
  }

  private def computeFilterState(inValveState: ValveState, outValveState: ValveState, filterState: FilterState) = {
    FilterState(filterState.id, filterState.health, outValveState.throughputTime, outValveState.totalThroughputTime, determineFilterStatus(inValveState, outValveState))
  }
}

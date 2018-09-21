package io.logbee.keyscore.agent.pipeline.controller

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.{ValvePosition, ValveProxy, ValveState}
import io.logbee.keyscore.model.{After, WhichValve}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The '''SourceController''' manages all requests for a running Source. <br><br>
  * He can change the Configuration of his Source on runtime, do live-edition operations or retrieve live-stats of the Source.
  *
  * @param source   The corresponding Source
  * @param outValve The Valve after the Source
  */
private class SourceController(val source: SourceProxy, val outValve: ValveProxy)(implicit val executionContext: ExecutionContext) extends Controller {

  override val id: UUID = source.id

  override def configure(configuration: Configuration): Future[FilterState] = {
    for {
      outValveState <- outValve.state()
      _ <- source.configure(configuration)
      sourceState <- source.state()
    } yield computeSourceState(outValveState, sourceState)
  }

  override def pause(doPause: Boolean): Future[FilterState] = {
    for {
      outValveState <- if (doPause) outValve.close() else outValve.open()
      sourceState <- source.state()
    } yield computeSourceState(outValveState, sourceState)
  }

  override def drain(drain: Boolean): Future[FilterState] = {
    for {
      outValveState <- if (drain) outValve.drain() else outValve.open()
      sourceState <- source.state()
    } yield computeSourceState(outValveState, sourceState)
  }

  override def insert(dataset: List[Dataset], whichValve: WhichValve): Future[FilterState] = {
    for {
      outValveState <- outValve.state()
      _ <- outValve.insert(dataset)
      sourceState <- source.state()
    } yield computeSourceState(outValveState, sourceState)
  }

  override def extract(n: Int, whichValve: WhichValve): Future[List[Dataset]] = {
    for {
      result <- outValve.extract(n)
    } yield result
  }

  override def state(): Future[FilterState] = {
    for {
      outValveState <- outValve.state()
      sourceState <- source.state()
    } yield computeSourceState(outValveState, sourceState)
  }

  override def clear(): Future[FilterState] = {
    for {
      outValveState <- outValve.state()
      sourceState <- source.state()
    } yield computeSourceState(outValveState, sourceState)
  }


  private def computeSourceState(outValveState: ValveState, sourceState: FilterState) = {
    FilterState(sourceState.id, sourceState.health, outValveState.throughputTime, outValveState.totalThroughputTime, determineSourceStatus(outValveState))
  }

  private def determineSourceStatus(out: ValveState): FilterStatus = {
    val status = out.position match {
      case ValvePosition.Closed => Paused
      case ValvePosition.Open => Running
      case ValvePosition.Drain => Dismantled
      case _ => Unknown
    }
    status
  }
}
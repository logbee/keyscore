package io.logbee.keyscore.agent.pipeline.controller

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.{ValvePosition, ValveProxy, ValveState}
import io.logbee.keyscore.model.WhichValve
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Label, TextValue}
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.model.notifications.NotificationsCollection
import io.logbee.keyscore.model.pipeline._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The '''SinkController''' manages all requests for a running Sink. <br><br>
  * He can change the Configuration of his Sink on runtime, do data-preview operations or retrieve live-stats of the Sink.
  *
  *
  * @param inValve The Valve before the Sink
  * @param sink The corresponding Sink
  */
private class SinkController(val inValve: ValveProxy, val sink: SinkProxy)(implicit executionContext: ExecutionContext) extends Controller {

  override val id: UUID = sink.id

  override def configure(configuration: Configuration): Future[FilterState] = {
    for {
      inValveState <- inValve.state()
      _ <- sink.configure(configuration)
      sinkState <- sink.state()
    } yield computeSinkState(inValveState, sinkState)
  }

  override def pause(doPause: Boolean): Future[FilterState] = {
    for {
      inValveState <- if (doPause) inValve.close() else inValve.open()
      sinkState <- sink.state()
    } yield computeSinkState(inValveState, sinkState)
  }

  override def drain(drain: Boolean): Future[FilterState] = {
    for {
      inValveState <- if (drain) inValve.drain() else inValve.open()
      sinkState <- sink.state()
    } yield computeSinkState(inValveState, sinkState)
  }

  override def insert(dataset: List[Dataset], whichValve: WhichValve): Future[FilterState] = {
    for {
      inValveState <- inValve.state()
      _ <- inValve.insert(dataset)
      sinkState <- sink.state()
    } yield computeSinkState(inValveState, sinkState)
  }

  override def extract(n: Int, whichValve: WhichValve): Future[List[Dataset]] = {
    for {
      result <- inValve.extract(n)
    } yield result
  }

  override def state(): Future[FilterState] = {
    for {
      inValveState <- inValve.state()
      sinkState <- sink.state()
    } yield computeSinkState(inValveState, sinkState)
  }

  override def scrapeMetrics(): Future[MetricsCollection] = {
    for {
      inValveMetrics <- inValve.scrape(Set(Label("port", TextValue("in"))))
      sinkMetrics <- sink.scrapeMetrics(Set(Label("port", TextValue("sink"))))
    } yield inValveMetrics ++ sinkMetrics
  }

  override def scrapeNotifications(): Future[NotificationsCollection] = {
    sink.scrapeNotifications
  }

  override def clear(): Future[FilterState] = {
    for {
      inValveState <- inValve.state()
      sinkState <- sink.state()
    } yield computeSinkState(inValveState, sinkState)
  }


  private def computeSinkState(inValveState: ValveState, sinkState: FilterState): FilterState = {
    FilterState(sinkState.id, sinkState.health, inValveState.throughputTime, inValveState.totalThroughputTime, determineSinkStatus(inValveState))
  }

  private def determineSinkStatus(out: ValveState): FilterStatus = {
    val status = out.position match {
      case ValvePosition.Closed => Paused
      case ValvePosition.Open => Running
      case ValvePosition.Drain => Dismantled
      case _ => Unknown
    }
    status
  }
}

package io.logbee.keyscore.frontier.sinks

import java.util.Locale

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Sink
import io.logbee.keyscore.frontier.filters.CommittableRecord
import io.logbee.keyscore.model.filter.{FilterConnection, FilterDescriptor}

import scala.concurrent.Future

object StdOutSink {

  def create(): Sink[CommittableRecord,Future[Done]] = {
    Sink.foreach[CommittableRecord](x => println(x.payload))
  }

  def descriptor: Locale => FilterDescriptor = {
    (language: Locale) => {
      FilterDescriptor("StdOutSink", "Standard Output Sink", "Writes the streams output to StdOut",
        FilterConnection(true), FilterConnection(false), List.empty, "Sink")
    }
  }
}

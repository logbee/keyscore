package io.logbee.keyscore.frontier.sinks

import java.util.Locale

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Sink
import io.logbee.keyscore.frontier.filters.CommittableRecord
import io.logbee.keyscore.model.filter.FilterConnection
import io.logbee.keyscore.model.sink.FilterDescriptor

import scala.collection.mutable
import scala.concurrent.Future

object StdOutSink {

  def create(): Sink[CommittableRecord, Future[Done]] = {
    Sink.foreach[CommittableRecord](x => println(x.payload))
  }

  def getDescriptors: mutable.Map[Locale, FilterDescriptor] = {
    val descriptors = mutable.Map.empty[Locale, FilterDescriptor]
    descriptors ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH)
    )
  }

  def descriptor(language: Locale): FilterDescriptor = {
    FilterDescriptor("StdOutSink", "Standard Output Sink", "Writes the streams output to StdOut",
      FilterConnection(true), FilterConnection(false), List.empty, "Sink")
  }
}

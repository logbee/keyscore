package io.logbee.keyscore.frontier.sinks

import java.util.Locale
import java.util.UUID.fromString

import akka.Done
import akka.stream.scaladsl.Sink
import io.logbee.keyscore.frontier.filters.CommittableRecord
import io.logbee.keyscore.model.filter.{FilterConnection, FilterDescriptorFragment, MetaFilterDescriptor}

import scala.collection.mutable
import scala.concurrent.Future

object StdOutSink {

  def create(): Sink[CommittableRecord, Future[Done]] = {
    Sink.foreach[CommittableRecord](x => println(x.payload))
  }
 val filterName ="StdOutSink"
 val filterId="d27a3b01-9f5f-4999-b345-bc01005355ed"

  def getDescriptors:MetaFilterDescriptor = {
    val descriptors = mutable.Map.empty[Locale, FilterDescriptorFragment]
    descriptors ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH)
    )
    MetaFilterDescriptor(fromString(filterId), filterName, descriptors.toMap)

  }

  def descriptor(language: Locale): FilterDescriptorFragment = {
    FilterDescriptorFragment("Standard Output Sink", "Writes the streams output to StdOut",
      FilterConnection(true), FilterConnection(false), List.empty, "Sink")
  }
}

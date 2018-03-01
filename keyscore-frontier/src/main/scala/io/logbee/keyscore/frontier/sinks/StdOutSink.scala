package io.logbee.keyscore.frontier.sinks

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Sink
import io.logbee.keyscore.frontier.filters.CommittableRecord
import io.logbee.keyscore.model.filter.FilterDescriptor

import scala.concurrent.Future

object StdOutSink {

  def create(): Sink[CommittableRecord,Future[Done]] = {
    Sink.foreach[CommittableRecord](x => println(x.payload))
  }

  val descriptor:FilterDescriptor = {
    FilterDescriptor("StdOutSink","Standard Output Sink","Writes the streams output to StdOut",List.empty)
  }
}

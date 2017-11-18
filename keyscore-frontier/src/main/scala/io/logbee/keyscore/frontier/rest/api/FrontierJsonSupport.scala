package io.logbee.keyscore.frontier.rest.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.logbee.keyscore.frontier.{Filter, Filters, Stream, Streams}
import spray.json.DefaultJsonProtocol

trait FrontierJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val streamFormat = jsonFormat2(Stream)
  implicit val streamsFormat = jsonFormat1(Streams)
  implicit val filterFormat = jsonFormat1(Filter)
  implicit val filtersFormat = jsonFormat1(Filters)
}

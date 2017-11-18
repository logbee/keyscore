package io.logbee.keyscore.frontier

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Stream(name: String, description: String)
case class Streams(streams: Seq[String])

case class Filter(name: String)
case class Filters(filters: Seq[String])



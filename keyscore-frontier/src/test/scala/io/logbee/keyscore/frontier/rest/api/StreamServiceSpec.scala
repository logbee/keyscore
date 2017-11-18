package io.logbee.keyscore.frontier.rest.api

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

class StreamServiceSpec extends TestKit(ActorSystem("StreamServiceSpec")) with WordSpecLike with Matchers {

  implicit val timeout: Timeout = 1.seconds

}

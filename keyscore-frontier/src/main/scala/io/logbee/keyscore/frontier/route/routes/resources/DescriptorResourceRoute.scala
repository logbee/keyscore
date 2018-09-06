package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.route.routes.DescriptorRoute.DescriptorRouteRequest
import io.logbee.keyscore.frontier.route.routes.resources.DescriptorResourceRoute.DescriptorResourceRouteResponse
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.json4s.native.Serialization

import scala.concurrent.duration._

object DescriptorResourceRoute {
  case object DescriptorResourceRouteRequest
  case object DescriptorResourceRouteResponse
}

class DescriptorResourceRoute extends Actor with ActorLogging with Json4sSupport {

  implicit val timeout: Timeout = 30 seconds
  implicit val system = context.system
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  override def receive: Receive = {
    case DescriptorRouteRequest =>
      sender ! DescriptorResourceRouteResponse
  }
}

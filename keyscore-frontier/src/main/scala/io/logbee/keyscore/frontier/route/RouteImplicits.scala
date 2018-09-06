package io.logbee.keyscore.frontier.route

trait RouteImplicits {

  import akka.util.Timeout
  import io.logbee.keyscore.model.json4s.KeyscoreFormats
  import org.json4s.native.Serialization

  import scala.concurrent.duration._

  implicit val timeout: Timeout = 30 seconds
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

}



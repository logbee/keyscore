package io.logbee.keyscore.frontier.route

import de.heikoseeberger.akkahttpjson4s.Json4sSupport

/** Implicits values for all Routes. <br><br>
  * `timeout` | `serialization` | `formats` <br>
  * __extends__ `Json4sSupport`
  */
trait RouteImplicits extends Json4sSupport{

  import akka.util.Timeout
  import io.logbee.keyscore.model.json4s.KeyscoreFormats
  import org.json4s.native.Serialization

  import scala.concurrent.duration._

  implicit val timeout: Timeout = 30 seconds
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

}



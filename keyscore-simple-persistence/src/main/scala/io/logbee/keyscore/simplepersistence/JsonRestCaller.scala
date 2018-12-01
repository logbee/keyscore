package io.logbee.keyscore.simplepersistence
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.collection.immutable.Seq
import scala.concurrent.Future

object JsonRestCaller{
  def apply(endpoint: String)(implicit actorSystem : ActorSystem ): JsonRestCaller = new JsonRestCaller(endpoint)
}


class JsonRestCaller(val endpoint:String)(implicit val actorSystem : ActorSystem) {

  private case class Example(var1: Int, var2: String)

  def post(request: String, content: String): Future[HttpResponse] =
    Http().singleRequest(createRequest(request, content, HttpMethods.POST))

  def get(request: String, content: String=""): Future[HttpResponse] =
    Http().singleRequest(createRequest(request, content, HttpMethods.GET))

  def put(request: String, content: String): Future[HttpResponse] =
    Http().singleRequest(createRequest(request, content, HttpMethods.PUT))

  private def createRequest(request: String, content: String, methodArg : HttpMethod): HttpRequest =
  HttpRequest(
    method = methodArg,
    uri = endpoint+request,
    entity = HttpEntity(ContentTypes.`application/json`, content),
    headers = Seq()
  )

}
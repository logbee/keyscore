package io.logbee.keyscore.frontier.sources

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, entity, onSuccess, pathEndOrSingleSlash, post}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.stream._
import akka.stream.stage._
import io.logbee.keyscore.frontier.filters.CommittableEvent
import io.logbee.keyscore.model.TextField

import scala.collection.mutable
import scala.concurrent.{Future, Promise}


class HttpSource(implicit val system: ActorSystem) extends GraphStageWithMaterializedValue[SourceShape[CommittableEvent], String] {

  private implicit val executionContext = system.dispatcher
  private implicit val materialzer = ActorMaterializer()

  private val out: Outlet[CommittableEvent] = Outlet("http.out")

  override val shape: SourceShape[CommittableEvent] = SourceShape(out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, String) = {
    (new HttpSourceLogic, "UniqueKillSwitch")
  }

  private class HttpSourceLogic extends GraphStageLogic(shape) with OutHandler {

    private val queue = mutable.Queue[String]()

    private val insertPayloadCallback = getAsyncCallback[(String, Promise[Boolean])] {
      case (payload, promise) =>
        if (isAvailable(out)) {
          push(out, CommittableEvent(TextField("message", payload)))
        }
        else {
          queue.enqueue(payload)
        }
        promise.success(true)
    }

    private val route: Route = pathEndOrSingleSlash {
      post {
        entity(as[String]) { payload =>
          val promise = Promise[Boolean]()
          insertPayloadCallback.invoke(payload, promise)
          onSuccess(promise.future) {
            case true => RouteDirectives.complete(StatusCodes.OK)
            case _ => RouteDirectives.complete(StatusCodes.InternalServerError)
          }
        }
      }
    }

    private var bindingFuture: Option[Future[Http.ServerBinding]] = None

    setHandler(out, this)

    override def preStart(): Unit = {
      bindingFuture = Option(Http().bindAndHandle(route, "localhost", 4321))
    }

    override def postStop(): Unit = {
      bindingFuture.foreach(_.flatMap(_.unbind()))
    }

    override def onPull(): Unit = {
      if (queue.nonEmpty) {
        push(out, CommittableEvent(TextField("message", queue.dequeue())))
      }
    }
  }
}


package io.logbee.keyscore.frontier.sources

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, entity, onSuccess, pathEndOrSingleSlash, post}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.stream.stage._
import akka.stream.{KillSwitch, _}
import io.logbee.keyscore.frontier.filters.CommittableEvent
import io.logbee.keyscore.model.{Event, TextField}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}


class HttpSource(initialConfiguration: HttpSourceConfiguration)(implicit val system: ActorSystem) extends GraphStageWithMaterializedValue[SourceShape[CommittableEvent], KillSwitch] {

  private implicit val executionContext = system.dispatcher
  private implicit val materialzer = ActorMaterializer()

  private val out: Outlet[CommittableEvent] = Outlet("http.out")

  override val shape: SourceShape[CommittableEvent] = SourceShape(out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, KillSwitch) = {
    val logic = new HttpSourceLogic
    (logic, logic.killSwitch)
  }

  private class HttpSourceLogic extends GraphStageLogic(shape) with OutHandler {

    private var fieldName: String = "message"
    private var bindAdress: String = "localhost"
    private var port: Int = 8000

    private val queue = mutable.Queue[CommittableEvent]()
    private var bindingFuture: Option[Future[Http.ServerBinding]] = None

    private val insertPayloadCallback = getAsyncCallback[(String, Promise[Event])] {
      case (payload, promise) =>
        val event = CommittableEvent(TextField(fieldName, payload))
        queue.enqueue(event)
        drain()
        promise.success(event)
    }

    val killSwitch: KillSwitch = new KillSwitch {

      override def abort(throwable: Throwable): Unit = {
        shutdown(Option(throwable))
      }

      override def shutdown(): Unit = {
        shutdown(None)
      }

      private def shutdown(throwable: Option[Throwable]): Unit = {
        val promise = Promise[Done]

        getAsyncCallback[(Option[Throwable], Promise[Done])] {
          case (exception, promise) =>
            exception match {
              case Some(throwable) => failStage(throwable)
              case None => completeStage()
            }
            promise.success(Done)
        }.invoke(throwable, promise)

        //Await.ready(promise.future, 5 seconds) // TODO: Do i have to wait until the stage is down?
      }
    }

    private val route: Route = pathEndOrSingleSlash {
      post {
        entity(as[String]) { payload =>
          val promise = Promise[Event]()
          insertPayloadCallback.invoke(payload, promise)
          onSuccess(promise.future) {
            case event => RouteDirectives.complete(StatusCodes.OK, s"${event.id}")
            case _ => RouteDirectives.complete(StatusCodes.InternalServerError)
          }
        }
      }
    }

    setHandler(out, this)

    override def preStart(): Unit = {
      update(initialConfiguration)
      bindingFuture = Option(Http().bindAndHandle(route, bindAdress, port))
    }

    override def postStop(): Unit = {
      bindingFuture.foreach(_.flatMap(_.unbind()))
    }

    override def onPull(): Unit = {
      drain()
    }

    private def drain(): Unit = {
      while (isAvailable(out) && queue.nonEmpty) {
        push(out, queue.dequeue())
      }
    }

    private def update(configuration: HttpSourceConfiguration): Unit = {
      bindAdress = configuration.bindAddress.getOrElse(bindAdress)
      port = configuration.port.getOrElse(port)
      fieldName = configuration.fieldName.getOrElse(fieldName)
    }
  }
}


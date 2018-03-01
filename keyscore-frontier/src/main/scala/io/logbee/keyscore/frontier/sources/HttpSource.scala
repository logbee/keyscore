package io.logbee.keyscore.frontier.sources

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, entity, onSuccess, pathEndOrSingleSlash, post}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.stage._
import akka.stream.{KillSwitch, _}
import io.logbee.keyscore.frontier.filters.CommittableRecord
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, IntParameterDescriptor, TextParameterDescriptor}
import io.logbee.keyscore.model.{Record, TextField}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

object HttpSource {

  def create(config: FilterConfiguration,system:ActorSystem): Source[CommittableRecord, UniqueKillSwitch] = {
    val httpConfig = try {
      loadFilterConfiguration(config)
    } catch {
      case nse: NoSuchElementException => throw nse
    }
    val httpSource=Source.fromGraph(new HttpSource(httpConfig)(system))
    httpSource.viaMat(KillSwitches.single)(Keep.right)
  }

  private def loadFilterConfiguration(configuration: FilterConfiguration): HttpSourceConfiguration = {
    try {
      val bindAddress = configuration.getParameterValue[String]("bindAddress")
      val port = configuration.getParameterValue[Int]("port")
      val fieldName = configuration.getParameterValue[String]("fieldName")
      HttpSourceConfiguration(Some(bindAddress), Some(port), Some(fieldName))
    } catch {
      case _: NoSuchElementException => throw new NoSuchElementException("Missing parameter in HttpSource configuration")
    }
  }

  val descriptor: FilterDescriptor = {
    FilterDescriptor("HttpSource", "Http Source", "A Http Source", List(
      TextParameterDescriptor("bindAddress"),
      TextParameterDescriptor("fieldName"),
      IntParameterDescriptor("port")

    ))
  }
}

class HttpSource(initialConfiguration: HttpSourceConfiguration)(implicit val system: ActorSystem) extends GraphStage[SourceShape[CommittableRecord]] {

  private implicit val executionContext = system.dispatcher
  private implicit val materialzer = ActorMaterializer()

  private val out: Outlet[CommittableRecord] = Outlet("http.out")

  override val shape: SourceShape[CommittableRecord] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): (GraphStageLogic) = {
    val logic = new HttpSourceLogic
    (logic)
  }

  private class HttpSourceLogic extends GraphStageLogic(shape) with OutHandler {

    private var fieldName: String = "message"
    private var bindAddress: String = "localhost"
    private var port: Int = 8000

    private val queue = mutable.Queue[CommittableRecord]()
    private var bindingFuture: Option[Future[Http.ServerBinding]] = None

    private val insertPayloadCallback = getAsyncCallback[(String, Promise[Record])] {
      case (payload, promise) =>
        val record = CommittableRecord(TextField(fieldName, payload))
        queue.enqueue(record)
        drain()
        promise.success(record)
    }


    /*val killSwitch: KillSwitch = new KillSwitch {

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
    }*/

    private val route: Route = pathEndOrSingleSlash {
      post {
        entity(as[String]) { payload =>
          val promise = Promise[Record]()
          insertPayloadCallback.invoke(payload, promise)
          onSuccess(promise.future) {
            case record => RouteDirectives.complete(StatusCodes.OK, s"${record.id}")
            case _ => RouteDirectives.complete(StatusCodes.InternalServerError)
          }
        }
      }
    }

    setHandler(out, this)

    override def preStart(): Unit = {
      update(initialConfiguration)
      bindingFuture = Option(Http().bindAndHandle(route, bindAddress, port))
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
      bindAddress = configuration.bindAddress.getOrElse(bindAddress)
      port = configuration.port.getOrElse(port)
      fieldName = configuration.fieldName.getOrElse(fieldName)
    }
  }

}


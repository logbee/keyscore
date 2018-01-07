package io.logbee.keyscore.frontier.filters

import akka.stream
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}

import scala.concurrent.{Future, Promise}

object RetainFieldsFilter {

  def apply(fieldNames: List[String]): Flow[CommittableEvent, CommittableEvent, Future[FilterHandle]] =
    Flow.fromGraph(new RetainFieldsFilter(fieldNames))
}

class RetainFieldsFilter(fieldNames: List[String]) extends Filter {
  implicit val formats = org.json4s.DefaultFormats

  val in = Inlet[CommittableEvent]("extr.in")
  val out = stream.Outlet[CommittableEvent]("extr.out")

  override val shape = FlowShape.of(in, out)

  override def createLogicAndMaterializedValue(attr: Attributes): (GraphStageLogic, Future[FilterHandle]) = {
    val logic = new RetainFieldsFilterLogic(shape)
    (logic, logic.promise.future)

  }

  private class RetainFieldsFilterLogic(shape: Shape) extends GraphStageLogic(shape) {

    val promise = Promise[FilterHandle]

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val event = grab(in)
        val payload = event.payload.filterKeys(fieldNames.contains(_))
        push(out, CommittableEvent(event.id, payload, event.offset))
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        pull(in)
      }
    })

    override def preStart(): Unit = {
      promise.success(null)
    }

  }

}

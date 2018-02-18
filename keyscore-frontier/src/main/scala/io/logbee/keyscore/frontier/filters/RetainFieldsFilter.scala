package io.logbee.keyscore.frontier.filters

import akka.stream
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}
import io.logbee.keyscore.model.filter.{FilterDescriptor, ListParameterDescriptor, TextParameterDescriptor}

import scala.concurrent.{Future, Promise}

object RetainFieldsFilter {

  def apply(fieldNames: List[String]): Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]] =
    Flow.fromGraph(new RetainFieldsFilter(fieldNames))

  val descriptor: FilterDescriptor = {
    FilterDescriptor("StandardRetainFieldsFilter", "RetainFieldsFilter", "Retains only the given fieldNames and their values and removes the other fields.", parameters = List(
      ListParameterDescriptor("fieldNames", TextParameterDescriptor("fieldName"), min = 1)
    ))
  }
}

class RetainFieldsFilter(fieldNames: List[String]) extends Filter {
  implicit val formats = org.json4s.DefaultFormats

  val in = Inlet[CommittableRecord]("extr.in")
  val out = stream.Outlet[CommittableRecord]("extr.out")

  override val shape = FlowShape.of(in, out)

  override def createLogicAndMaterializedValue(attr: Attributes): (GraphStageLogic, Future[FilterHandle]) = {
    val logic = new RetainFieldsFilterLogic(shape)
    (logic, logic.promise.future)

  }

  private class RetainFieldsFilterLogic(shape: Shape) extends GraphStageLogic(shape) {

    val promise = Promise[FilterHandle]

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val record = grab(in)
        val payload = record.payload.filterKeys(fieldNames.contains(_))
        push(out, CommittableRecord(record.id, payload, record.offset))
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

package io.logbee.keyscore.frontier.filters

import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}
import akka.{NotUsed, stream}
import io.logbee.keyscore.model.filter.FilterDescriptor.FilterDescriptor
import io.logbee.keyscore.model.filter.{ListParameterDescriptor, TextParameterDescriptor}

import scala.concurrent.{Future, Promise}

object RemoveFieldsFilter {
  def apply(fieldNames: List[String]): Flow[CommittableEvent, CommittableEvent, Future[FilterHandle]] =
    Flow.fromGraph(new RemoveFieldsFilter(fieldNames))

  val descriptor: FilterDescriptor = {
    FilterDescriptor("StandardRemoveFieldsFilter", "RemoveFieldsFilter","Removes fieldNames and their values.", List(
      ListParameterDescriptor("fieldNames", TextParameterDescriptor("fieldName"), min = 1)
    ))
  }
}

class RemoveFieldsFilter(fieldNames: List[String]) extends Filter {
  implicit val formats = org.json4s.DefaultFormats

  val in = Inlet[CommittableEvent]("removeFields.in")
  val out = stream.Outlet[CommittableEvent]("removeFields.out")

  override val shape = FlowShape.of(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[FilterHandle]) = {
    val logic = new RemoveFieldsFilterLogic(shape)
    (logic, logic.promise.future)
  }

  private class RemoveFieldsFilterLogic(shape: Shape) extends GraphStageLogic(shape) {
    val promise = Promise[FilterHandle]

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val event = grab(in)
        var payload = event.payload.filterKeys(!fieldNames.contains(_))

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
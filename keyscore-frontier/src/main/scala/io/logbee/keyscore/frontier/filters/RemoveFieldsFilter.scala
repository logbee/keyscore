package io.logbee.keyscore.frontier.filters

import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}
import akka.{NotUsed, stream}

object RemoveFieldsFilter {
  def apply(fieldNames: List[String]): Flow[CommittableEvent, CommittableEvent, NotUsed] =
    Flow.fromGraph(new RemoveFieldsFilter(fieldNames))
}

class RemoveFieldsFilter(fieldNames: List[String]) extends Filter {
  implicit val formats = org.json4s.DefaultFormats

  val in = Inlet[CommittableEvent]("removeFields.in")
  val out = stream.Outlet[CommittableEvent]("removeFields.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

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
    }

  }
}
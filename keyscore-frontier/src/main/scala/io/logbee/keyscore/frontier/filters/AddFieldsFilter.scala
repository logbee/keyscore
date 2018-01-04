package io.logbee.keyscore.frontier.filters

import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}
import akka.{NotUsed, stream}
import io.logbee.keyscore.model.{Field, TextField}
import org.json4s.DefaultFormats

object AddFieldsFilter {
  def apply(fieldsToAdd: Map[String, String]): Flow[CommittableEvent, CommittableEvent, NotUsed] =
    Flow.fromGraph(new AddFieldsFilter(fieldsToAdd))
}

class AddFieldsFilter(fieldsToAdd: Map[String, String]) extends Filter {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val in = Inlet[CommittableEvent]("addFields.in")
  val out = stream.Outlet[CommittableEvent]("addFields.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val event = grab(in)
          var payload = scala.collection.mutable.Map[String, Field]()

          payload ++= event.payload
          payload ++= fieldsToAdd.map(pair => (pair._1, TextField(pair._1, pair._2)))

          push(out, CommittableEvent(event.id, payload.toMap, event.offset))
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

package io.logbee.keyscore.frontier.filters

import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}
import akka.{NotUsed, stream}
import io.logbee.keyscore.model.filter.{FilterDescriptor, MapParameterDescriptor, TextParameterDescriptor}
import io.logbee.keyscore.model.{Field, TextField}
import org.json4s.DefaultFormats

import scala.concurrent.{Future, Promise}

object AddFieldsFilter {
  def apply(fieldsToAdd: Map[String, String]): Flow[CommittableEvent, CommittableEvent, Future[FilterHandle]] =
    Flow.fromGraph(new AddFieldsFilter(fieldsToAdd))

  val descriptor: FilterDescriptor = {
    FilterDescriptor("AddFieldsFilter", "Adding new fields and their values.", List(
      MapParameterDescriptor("fieldNames", TextParameterDescriptor("fieldNames"), TextParameterDescriptor("fieldValues"), 1)
    ))
  }
}

class AddFieldsFilter(fieldsToAdd: Map[String, String]) extends Filter {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val in = Inlet[CommittableEvent]("addFields.in")
  val out = stream.Outlet[CommittableEvent]("addFields.out")

  override val shape = FlowShape.of(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[FilterHandle]) = {
    val logic = new AddFieldsFilterLogic(shape)
    (logic, logic.promise.future)
  }

  private class AddFieldsFilterLogic(shape: Shape) extends GraphStageLogic(shape) {
    val promise = Promise[FilterHandle]

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

    override def preStart(): Unit = {
      promise.success(DummyFilterHandle)
    }
  }
}
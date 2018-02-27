package io.logbee.keyscore.frontier.filters

import akka.stream
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, MapParameterDescriptor, TextParameterDescriptor}
import io.logbee.keyscore.model.{Field, TextField}
import org.json4s.DefaultFormats

import scala.concurrent.{Future, Promise}

object AddFieldsFilter {
  def apply(fieldsToAdd: Map[String, String]): Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]] = {
    Flow.fromGraph(new AddFieldsFilter(fieldsToAdd))
  }

  def create(config: FilterConfiguration): Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]] = {
    val addConfiguration = try {
      loadFilterConfiguration(config)
    } catch {
      case nse: NoSuchElementException => throw nse
    }
    Flow.fromGraph(new AddFieldsFilter(addConfiguration))
  }

  private def loadFilterConfiguration(config: FilterConfiguration): Map[String, String] = {
    try {
      config.getParameterValue[Map[String, String]]("fieldsToAdd")
    } catch {
      case _: NoSuchElementException => throw new NoSuchElementException("AddFieldsFilter needs parameter: fieldsToAdd of type map[string,string] ")
    }
  }

  val descriptor: FilterDescriptor = {
    FilterDescriptor("AddFieldsFilter", "Add Fields Filter", "Adding new fields and their values.", List(
      MapParameterDescriptor("fieldsToAdd", TextParameterDescriptor("fieldName"), TextParameterDescriptor("fieldValue"), 1)
    ))
  }
}

class AddFieldsFilter(fieldsToAdd: Map[String, String]) extends Filter {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val in = Inlet[CommittableRecord]("addFields.in")
  val out = stream.Outlet[CommittableRecord]("addFields.out")

  override val shape = FlowShape.of(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[FilterHandle]) = {
    val logic = new AddFieldsFilterLogic(shape)
    (logic, logic.promise.future)
  }

  private class AddFieldsFilterLogic(shape: Shape) extends GraphStageLogic(shape) {
    val promise = Promise[FilterHandle]

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val record = grab(in)
        var payload = scala.collection.mutable.Map[String, Field]()

        payload ++= record.payload
        payload ++= fieldsToAdd.map(pair => (pair._1, TextField(pair._1, pair._2)))

        push(out, CommittableRecord(record.id, payload.toMap, record.offset))
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
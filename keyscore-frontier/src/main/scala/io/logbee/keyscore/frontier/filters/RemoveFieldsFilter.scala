package io.logbee.keyscore.frontier.filters

import akka.stream
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}
import io.logbee.keyscore.model.filter._

import scala.concurrent.{Future, Promise}

object RemoveFieldsFilter {
  def apply(fieldNames: List[String]): Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]] = {
    Flow.fromGraph(new RemoveFieldsFilter(fieldNames))
  }

  def create(config: FilterConfiguration): Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]] = {
    val removeFieldsConfig = try {
      loadFilterConfiguration(config)
    } catch {
      case nse: NoSuchElementException => throw nse
    }
    Flow.fromGraph(new RemoveFieldsFilter(removeFieldsConfig))
  }

  private def loadFilterConfiguration(configuration: FilterConfiguration): List[String] = {
    try {
      configuration.getParameterValue[List[String]]("fieldsToRemove")
    } catch {
      case _: NoSuchElementException => throw new NoSuchElementException("RemoveFieldsFilter needs parameter: fieldsToRemove of type list[string]")
    }
  }

  val descriptor: FilterDescriptor = {
    FilterDescriptor("RemoveFieldsFilter", "Remove Fields Filter", "Removes all given fields and their values.",
      FilterConnection(true),FilterConnection(true),List(
      ListParameterDescriptor("fieldsToRemove", TextParameterDescriptor("fieldName"), min = 1)
    ))
  }
}

class RemoveFieldsFilter(fieldNames: List[String]) extends Filter {
  implicit val formats = org.json4s.DefaultFormats

  val in = Inlet[CommittableRecord]("removeFields.in")
  val out = stream.Outlet[CommittableRecord]("removeFields.out")

  override val shape = FlowShape.of(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[FilterHandle]) = {
    val logic = new RemoveFieldsFilterLogic(shape)
    (logic, logic.promise.future)
  }

  private class RemoveFieldsFilterLogic(shape: Shape) extends GraphStageLogic(shape) {
    val promise = Promise[FilterHandle]

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val record = grab(in)
        var payload = record.payload.filterKeys(!fieldNames.contains(_))

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
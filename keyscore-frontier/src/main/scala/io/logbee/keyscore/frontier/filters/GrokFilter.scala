package io.logbee.keyscore.frontier.filters

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import io.logbee.keyscore.model.filter.{BooleanParameterDescriptor, FilterDescriptor, ListParameterDescriptor, TextParameterDescriptor}
import io.logbee.keyscore.model.{Field, TextField}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.matching.Regex


object GrokFilter {

  def apply(config: GrokFilterConfiguration): Flow[CommittableEvent, CommittableEvent, Future[FilterHandle]] =
    Flow.fromGraph(new GrokFilter(config))

  def apply(isPaused: Boolean, fieldNames: List[String], pattern: String):  Flow[CommittableEvent, CommittableEvent, Future[FilterHandle]] = {
    val conf = new GrokFilterConfiguration(Option(isPaused), Option(fieldNames), Option(pattern))
    Flow.fromGraph(new GrokFilter(conf))
  }

  val descriptor: FilterDescriptor = {
    FilterDescriptor("GrokFilter", description = "Extracts parts of a text line into fields.", parameters = List(
      BooleanParameterDescriptor("isPaused"),
      ListParameterDescriptor("fieldNames", TextParameterDescriptor("field"), min = 1),
      TextParameterDescriptor("pattern")
    ))
  }
}

class GrokFilter(config: GrokFilterConfiguration) extends Filter {

  implicit val formats: DefaultFormats.type = DefaultFormats

  val in = Inlet[CommittableEvent]("grok.in")
  val out = Outlet[CommittableEvent]("grok.out")

  override val shape = FlowShape(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[GrokFilterHandle]) = {
    val logic = new GrokFilterLogic(shape, config)
    (logic, logic.promise.future)
  }

  private class GrokFilterLogic(shape: Shape, val initialConfig: GrokFilterConfiguration) extends GraphStageLogic(shape) with InHandler with OutHandler {

    val promise = Promise[GrokFilterHandle]

    private val groupNamePattern: Regex = "\\(\\?<(\\w*)>".r
    private var isPaused = false
    private var fieldNames = List.empty[String]
    private var regex: Regex = "".r

    private val handle = new GrokFilterHandle {
      private val callback = getAsyncCallback[(GrokFilterConfiguration, Promise[Boolean])] {
        case (newConfig, promise) =>

          val currentlyPaused = isPaused

          update(newConfig)

          if (currentlyPaused && !isPaused) {
            if (isAvailable(in)) {
              val input = grab(in)
              push(out, filter(input))
            } else if (isAvailable(out) && !hasBeenPulled(in)) {
              pull(in)
            }
          }

          promise.success(true)
      }

      override def configure(config: GrokFilterConfiguration): Future[Boolean] = {
        val promise = Promise[Boolean]()
        callback.invoke((config, promise))
        promise.future
      }
    }

    setHandlers(in, out, this)

    override def preStart(): Unit = {
      update(initialConfig)
      promise.success(handle)
    }

    def filter(event: CommittableEvent): CommittableEvent = {

      val payload = new mutable.HashMap[String, Field]

      for (field <- event.payload.values) {
        payload.put(field.name, field)
        if (fieldNames.contains(field.name) && field.isInstanceOf[TextField]) {
          regex.findFirstMatchIn(field.asInstanceOf[TextField].value)
            .foreach(m => m.groupNames.map(name => TextField(name, m.group(name)))
              .foreach(textField => payload.put(textField.name, textField)))
        }
      }
      new CommittableEvent(event.id, payload.toMap, event.offset)
    }

    override def onPush(): Unit = {
      if (isOpen) {
        val input = grab(in)
        push(out, filter(input))
      }
    }

    override def onPull(): Unit = {
      if (isOpen) {
        pull(in)
      }
    }

    private def isOpen = !isPaused

    private def update(config: GrokFilterConfiguration): Unit = {
      isPaused = config.isPaused.getOrElse(isPaused)
      fieldNames = config.fieldNames.getOrElse(fieldNames)
      config.pattern match {
        case Some(pattern) =>
          regex = pattern.r(groupNamePattern.findAllMatchIn(pattern).map(_.group(1)).toSeq: _*)
        case None =>
      }
    }
  }

}

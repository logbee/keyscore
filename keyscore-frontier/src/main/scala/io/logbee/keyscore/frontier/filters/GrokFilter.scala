package io.logbee.keyscore.frontier.filters

import akka.stream._
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import io.logbee.keyscore.model.{Event, Field, TextField}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.matching.Regex


object GrokFilter {

  def apply(): GrokFilter = GrokFilter(GrokFilterConfiguration(
    isPaused = Option(true)
  ))

  def apply(config: GrokFilterConfiguration): GrokFilter = new GrokFilter(config)
}

class GrokFilter(config: GrokFilterConfiguration) extends Filter {

  val in = Inlet[CommittableEvent]("grok.in")
  val out = Outlet[CommittableEvent]("grok.out")

  override val shape = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    val logic = new GrokFilterLogic(shape, config)
//    val graph = (logic, logic.promise.future)
    logic
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
              push(out, filter(grab(in)))
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

    def filter(event: Event): Event = {

      val payload = new mutable.HashMap[String, Field]

      for (field <- event.payload.values) {
        payload.put(field.name, field)
        if (fieldNames.contains(field.name) && field.isInstanceOf[TextField]) {
          regex.findFirstMatchIn(field.asInstanceOf[TextField].value)
            .foreach(m => m.groupNames.map(name => TextField(name, m.group(name)))
              .foreach(textField => payload.put(textField.name, textField)))
        }
      }

      val id = grab(in).id
      val offset = grab(in).offset

      new CommittableEvent(id, payload.toMap, offset)
    }

    override def onPush(): Unit = {
      if (isOpen) {
        push(out, filter(grab(in)))
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

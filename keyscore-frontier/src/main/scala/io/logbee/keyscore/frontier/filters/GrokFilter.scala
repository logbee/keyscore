package io.logbee.keyscore.frontier.filters

import akka.stream._
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Field, NumberField, TextField}
import org.json4s.DefaultFormats

import scala.Function.tupled
import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.matching.Regex


object GrokFilter {

  def apply(config: GrokFilterConfiguration): Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]] =
    Flow.fromGraph(new GrokFilter(config))

  def apply(isPaused: Boolean, fieldNames: List[String], pattern: String): Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]] = {
    val conf = new GrokFilterConfiguration(Option(isPaused), Option(fieldNames), Option(pattern))
    Flow.fromGraph(new GrokFilter(conf))
  }

  def create(config: FilterConfiguration): Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]] = {
    val grokConfiguration = try {
      loadFilterConfiguration(config)
    } catch {
      case nse: NoSuchElementException => throw nse
    }
    Flow.fromGraph(new GrokFilter(grokConfiguration))
  }

  private def loadFilterConfiguration(config: FilterConfiguration): GrokFilterConfiguration = {
    try {
      val isPaused = config.getParameterValue[Boolean]("isPaused")
      val pattern = config.getParameterValue[String]("pattern")
      val fieldNames = config.getParameterValue[List[String]]("fieldNames")

      GrokFilterConfiguration(Option(isPaused), Option(fieldNames), Option(pattern))
    } catch {
      case _: NoSuchElementException => throw new NoSuchElementException("Missing parameter in GrokFilter configuration")
    }
  }

  val descriptor: FilterDescriptor = {
    FilterDescriptor("GrokFilter", "Grok Filter", "Extracts parts of a text line into fields.", FilterConnection(true, "all"),
      FilterConnection(true, "all"), List(
        BooleanParameterDescriptor("isPaused"),
        ListParameterDescriptor("fieldNames", TextParameterDescriptor("field"), min = 1),
        TextParameterDescriptor("pattern")
      ))
  }
}

class GrokFilter(initialConfiguration: GrokFilterConfiguration) extends Filter {

  implicit val formats: DefaultFormats.type = DefaultFormats

  val in = Inlet[CommittableRecord]("grok.in")
  val out = Outlet[CommittableRecord]("grok.out")

  override val shape = FlowShape(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[GrokFilterHandle]) = {
    val logic = new GrokFilterLogic
    (logic, logic.promise.future)
  }

  private class GrokFilterLogic extends GraphStageLogic(shape) with InHandler with OutHandler {

    val promise = Promise[GrokFilterHandle]

    private val GROK_PATTERN: Regex = "\\(\\?<(\\w*)>".r
    private val NUMBER_PATTERN: Regex = "^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$".r

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
      update(initialConfiguration)
      promise.success(handle)
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

    private def filter(record: CommittableRecord): CommittableRecord = {

      val payload = new mutable.HashMap[String, Field]
      for (field <- record.payload.values) {
        payload.put(field.name, field)
        if (fieldNames.contains(field.name) && field.isInstanceOf[TextField]) {
          regex.findFirstMatchIn(field.asInstanceOf[TextField].value)
            .foreach(patternMatch => patternMatch.groupNames.map(name => (name, patternMatch.group(name))) map tupled { (name, value) =>
              value match {
                case NUMBER_PATTERN(_*) => NumberField(name, BigDecimal(value))
                case _ => TextField(name, value)
              }
            } foreach (field => payload.put(field.name, field)))
        }
      }

      new CommittableRecord(record.id, payload.toMap, record.offset)
    }

    private def isOpen = !isPaused

    private def update(config: GrokFilterConfiguration): Unit = {
      isPaused = config.isPaused.getOrElse(isPaused)
      fieldNames = config.fieldNames.getOrElse(fieldNames)
      config.pattern match {
        case Some(pattern) =>
          regex = pattern.r(GROK_PATTERN.findAllMatchIn(pattern).map(_.group(1)).toSeq: _*)
        case None =>
      }
    }
  }

}

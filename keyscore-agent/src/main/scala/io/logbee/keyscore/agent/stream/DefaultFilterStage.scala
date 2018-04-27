package io.logbee.keyscore.agent.stream
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler, StageLogging}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter._

import scala.concurrent.{Future, Promise}

class DefaultFilterStage extends FilterStage {

  private val in = Inlet[Dataset]("grok.in")
  private val out = Outlet[Dataset]("grok.out")

  override def shape = FlowShape(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Filter]) = {
    val logic = new DefaultFilterLogic
    (logic, logic.promise.future)
  }

  private class DefaultFilterLogic extends GraphStageLogic(shape) with InHandler with OutHandler with StageLogging {

    val promise = Promise[Filter]

    private var configuration: FilterConfiguration = null
    private var condition: FilterCondition = noopCondition
    private var function: FilterFunction = noopFunction

    private val filter = new Filter {

      private val configureConditionCallback = getAsyncCallback[(Option[FilterCondition], Promise[Boolean])] {
        case (newCondition, promise) =>
          condition = newCondition.getOrElse(noopCondition)
          promise.success(true)
          log.info(s"New condition configured: ${condition.getClass.getName}")
      }

      private val configureFunctionCallback = getAsyncCallback[(Option[FilterFunction], Promise[Boolean])] {
        case (newFunction, promise) =>
          function = newFunction.getOrElse(noopFunction)
          promise.success(true)
          log.info(s"New function configured: ${function.getClass.getName}")
      }

      private val configureConfigurationCallback = getAsyncCallback[(Option[FilterConfiguration], Promise[Boolean])] {
        case (newConfiguration, promise) =>
          configuration = newConfiguration.getOrElse(configuration)
          condition.configure(configuration)
          function.configure(configuration)
          promise.success(true)
          log.info(s"New configuration: $configuration")
      }

      override def configure(trigger: FilterCondition): Future[Boolean] = {
        val promise = Promise[Boolean]()
        configureConditionCallback.invoke((Option(trigger), promise))
        promise.future
      }

      override def configure(function: FilterFunction): Future[Boolean] = {
        val promise = Promise[Boolean]()
        configureFunctionCallback.invoke(Option(function), promise)
        promise.future
      }

      override def configure(configuration: FilterConfiguration): Future[Boolean] = {
        val promise = Promise[Boolean]()
        configureConfigurationCallback.invoke(Option(configuration), promise)
        promise.future
      }
    }

    setHandlers(in, out, this)

    override def preStart(): Unit = {
      promise.success(filter)
    }

    override def onPull(): Unit = {
      pull(in)
    }

    override def onPush(): Unit = {
      condition(grab(in)) match {
        case Accept(dataset) => push(out, function(dataset))
        case Reject(dataset) => push(out, dataset)
        case _ => // drop
      }
    }
  }

  private val noopCondition = new FilterCondition {
    override def configure(configuration: FilterConfiguration): Boolean = { true }
    override def apply(dataset: Dataset): FilterConditionResult = { Reject(dataset) }
  }

  private val noopFunction = new FilterFunction {
    override def configure(configuration: FilterConfiguration): Boolean = { true }
    override def apply(dataset: Dataset): Dataset = { dataset }
  }
}

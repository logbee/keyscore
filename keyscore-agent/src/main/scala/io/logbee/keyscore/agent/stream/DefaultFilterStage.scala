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

    private var condition: FilterCondition = noopCondition
    private var function: FilterFunction = noopFunction

    private val filter = new Filter {

      private val changeConditionCallback = getAsyncCallback[(Option[FilterCondition], Promise[Boolean])] {
        case (newCondition, promise) =>
          condition = newCondition.getOrElse(noopCondition)
          promise.success(true)
          log.info(s"Condition changed: ${condition.getClass.getName}")
      }

      private val changeFunctionCallback = getAsyncCallback[(Option[FilterFunction], Promise[Boolean])] {
        case (newFunction, promise) =>
          function = newFunction.getOrElse(noopFunction)
          promise.success(true)
          log.info(s"Function changed: ${function.getClass.getName}")
      }

      private val configureConditionCallback = getAsyncCallback[(FilterConfiguration, Promise[Boolean])] {
        case (configuration, promise) =>
          condition.configure(configuration)
          promise.success(true)
          log.info(s"Condition configuration has been updated: $configuration")
      }

      private val configureFunctionCallback = getAsyncCallback[(FilterConfiguration, Promise[Boolean])] {
        case (configuration, promise) =>
          function.configure(configuration)
          promise.success(true)
          log.info(s"Function configuration has been updated: $configuration")
      }

      override def changeCondition(condition: FilterCondition): Future[Boolean] = {
        val promise = Promise[Boolean]()
        log.info(s"Changing condition: ${condition.getClass.getName}")
        changeConditionCallback.invoke((Option(condition), promise))
        promise.future
      }

      override def changeFunction(function: FilterFunction): Future[Boolean] = {
        val promise = Promise[Boolean]()
        log.info(s"Changing function: ${function.getClass.getName}")
        changeFunctionCallback.invoke(Option(function), promise)
        promise.future
      }

      override def configureCondition(configuration: FilterConfiguration): Future[Boolean] = {
        val promise = Promise[Boolean]()
        log.info(s"Configuring condition: $configuration")
        configureConditionCallback.invoke(configuration, promise)
        promise.future
      }

      override def configureFunction(configuration: FilterConfiguration): Future[Boolean] = {
        val promise = Promise[Boolean]()
        log.info(s"Configuring function: $configuration")
        configureFunctionCallback.invoke(configuration, promise)
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

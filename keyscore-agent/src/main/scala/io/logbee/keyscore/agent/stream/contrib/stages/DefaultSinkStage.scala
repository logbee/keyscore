package io.logbee.keyscore.agent.stream.contrib.stages

import akka.stream.stage.{GraphStageLogic, InHandler, StageLogging}
import akka.stream.{Attributes, Inlet, SinkShape}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.sink.{Sink, SinkFunction}
import io.logbee.keyscore.agent.stream.contrib.stages.DefaultSinkStage.{noopCondition, noopFunction}

import scala.concurrent.{Future, Promise}

object DefaultSinkStage {
  private val noopCondition = new Condition {
    override def configure(configuration: FilterConfiguration): Boolean = { true }
    override def apply(dataset: Dataset): ConditionResult = { Accept(dataset) }
  }

  private val noopFunction = new SinkFunction {
    override def configure(configuration: FilterConfiguration): Unit = {  }
    override def apply(dataset: Dataset): Unit = {  }
  }
}
class DefaultSinkStage(initialCondition: Condition = noopCondition, initialFunction: SinkFunction = noopFunction) extends SinkStage {

  private val in = Inlet[Dataset]("in")

  override def shape: SinkShape[Dataset] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Sink]) = {
    val logic = new DefaultSinkLogic
    (logic, logic.initPromise.future)
  }

  private class DefaultSinkLogic extends GraphStageLogic(shape) with InHandler with StageLogging {

    val initPromise = Promise[Sink]

    private var condition: Condition = initialCondition
    private var function: SinkFunction = initialFunction

    private val sink = new Sink {

      private val changeConditionCallback = getAsyncCallback[(Option[Condition], Promise[Unit])] {
        case (newCondition, promise) =>
          condition = newCondition.getOrElse(noopCondition)
          promise.success(())
          log.info(s"Condition changed: ${condition.getClass.getName}")
      }

      private val changeFunctionCallback = getAsyncCallback[(Option[SinkFunction], Promise[Unit])] {
        case (newFunction, promise) =>
          function = newFunction.getOrElse(noopFunction)
          promise.success(())
          log.info(s"Function changed: ${function.getClass.getName}")
      }

      private val configureConditionCallback = getAsyncCallback[(FilterConfiguration, Promise[Unit])] {
        case (configuration, promise) =>
          condition.configure(configuration)
          promise.success(())
          log.info(s"Condition configuration has been updated: $configuration")
      }

      private val configureFunctionCallback = getAsyncCallback[(FilterConfiguration, Promise[Unit])] {
        case (configuration, promise) =>
          function.configure(configuration)
          promise.success(())
          log.info(s"Function configuration has been updated: $configuration")
      }

      override def changeCondition(condition: Condition): Future[Unit] = {
        val promise = Promise[Unit]()
        log.info(s"Changing condition: ${condition.getClass.getName}")
        changeConditionCallback.invoke((Option(condition), promise))
        promise.future
      }

      override def changeFunction(function: SinkFunction): Future[Unit] = {
        val promise = Promise[Unit]()
        log.info(s"Changing function: ${function.getClass.getName}")
        changeFunctionCallback.invoke((Option(function), promise))
        promise.future
      }

      override def configureCondition(configuration: FilterConfiguration): Future[Unit] = {
        val promise = Promise[Unit]()
        log.info(s"Configuring condition: $configuration")
        configureConditionCallback.invoke(configuration, promise)
        promise.future
      }

      override def configureFunction(configuration: FilterConfiguration): Future[Unit] = {
        val promise = Promise[Unit]()
        log.info(s"Configuring function: $configuration")
        configureFunctionCallback.invoke(configuration, promise)
        promise.future
      }
    }

    setHandler(in, this)

    override def preStart(): Unit = {
      initPromise.success(sink)
      pull(in)
    }

    override def onPush(): Unit = {

      condition(grab(in)) match {
        case Accept(dataset) =>
          function(dataset)
          pull(in)
        case _ => // drop
      }
    }
  }
}

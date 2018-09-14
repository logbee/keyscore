package io.logbee.keyscore.pipeline.api

import akka.actor.ActorSystem
import akka.stream.stage.{GraphStageLogic, StageLogging}
import akka.stream.{Materializer, Shape}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.pipeline.{FilterState, LogicProxy}

import scala.concurrent.{ExecutionContextExecutor, Promise}

abstract class AbstractLogic[P <: LogicProxy](val parameters: LogicParameters, shape: Shape) extends GraphStageLogic(shape) with StageLogging {

  private[api] val initPromise = Promise[P]

  protected val proxy: P

  protected implicit val system: ActorSystem = parameters.context.system
  protected implicit val dispatcher: ExecutionContextExecutor = parameters.context.dispatcher
  protected override implicit lazy val materializer: Materializer = super.materializer

  override def preStart(): Unit = {
    log.info(s"Initializing <${parameters.uuid}> with configuration: ${parameters.configuration}")
    initialize(parameters.configuration)
    initPromise.success(proxy)
  }

  def initialize(configuration: Configuration): Unit

  def configure(configuration: Configuration): Unit

  def state(): FilterState
}

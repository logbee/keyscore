package io.logbee.keyscore.pipeline.api

import akka.stream.stage.{InHandler, StageLogging}
import akka.stream.{Inlet, SinkShape}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.pipeline.{FilterState, SinkProxy}

abstract class SinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset])
  extends AbstractLogic[SinkProxy](parameters, shape) with InHandler with StageLogging {

  override protected val proxy = new DefaultLogicProxy(this) with SinkProxy {}

  protected val in: Inlet[Dataset] = shape.in

  setHandler(in, this)

  def state(): FilterState = FilterState(parameters.uuid, Green)
}

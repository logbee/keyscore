package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.stage.{InHandler, StageLogging}
import akka.stream.{Inlet, SinkShape}
import io.logbee.keyscore.model.Green
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.{FilterState, SinkProxy}

abstract class SinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset])
  extends AbstractLogic[SinkProxy](parameters, shape) with InHandler with StageLogging {

  override protected val proxy = new DefaultLogicProxy(this) with SinkProxy {}

  protected val in: Inlet[Dataset] = shape.in

  setHandler(shape.in, this)

  def state(): FilterState = FilterState(parameters.uuid, Green)
}

package io.logbee.keyscore.pipeline.api

import akka.stream.stage.{InHandler, OutHandler, StageLogging}
import akka.stream.{FlowShape, Inlet, Outlet}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.pipeline.{FilterProxy, FilterState}

abstract class FilterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset])
  extends AbstractLogic[FilterProxy](parameters, shape) with InHandler with OutHandler with StageLogging {

  override protected val proxy = new DefaultLogicProxy(this) with FilterProxy {}

  protected val in: Inlet[Dataset] = shape.in
  protected val out: Outlet[Dataset] = shape.out

  setHandler(in, this)
  setHandler(out, this)

  def state(): FilterState = FilterState(parameters.uuid, Green)
}

package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.stage.{InHandler, OutHandler}
import akka.stream.{Inlet, Outlet}
import io.logbee.keyscore.model.Green
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.{BranchProxy, FilterState}

abstract class BranchLogic(parameters: LogicParameters, shape: BranchShape[Dataset, Dataset, Dataset])
  extends AbstractLogic[BranchProxy](parameters, shape) with InHandler with OutHandler {

  override protected val proxy = new DefaultLogicProxy(this) with BranchProxy {}

  protected val in: Inlet[Dataset] = shape.in
  protected val left: Outlet[Dataset] = shape.left
  protected val right: Outlet[Dataset] = shape.right

  setHandler(in, this)
  setHandler(left, this)
  setHandler(right, this)

  def state(): FilterState = FilterState(parameters.uuid, Green)
}

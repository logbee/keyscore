package io.logbee.keyscore.pipeline.api

import akka.stream.stage.{InHandler, OutHandler}
import akka.stream.{Inlet, Outlet}
import io.logbee.keyscore.model.Green
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.{FilterState, MergeProxy}

abstract class MergeLogic(parameters: LogicParameters, shape: MergeShape[Dataset, Dataset, Dataset])
  extends AbstractLogic[MergeProxy](parameters, shape) with InHandler with OutHandler {

  override protected val proxy = new DefaultLogicProxy(this) with MergeProxy {}

  protected val left: Inlet[Dataset] = shape.left
  protected val right: Inlet[Dataset] = shape.right
  protected val out: Outlet[Dataset] = shape.out

  setHandler(left, this)
  setHandler(right, this)
  setHandler(out, this)

  def state(): FilterState = FilterState(parameters.uuid, Green)
}

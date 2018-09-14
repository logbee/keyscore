package io.logbee.keyscore.pipeline.api

import akka.stream.stage.{OutHandler, StageLogging}
import akka.stream.{Outlet, SourceShape}
import io.logbee.keyscore.model.Green
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.{FilterState, SourceProxy}

abstract class SourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset])
  extends AbstractLogic[SourceProxy](parameters, shape) with OutHandler with StageLogging {

  override protected val proxy = new DefaultLogicProxy(this) with SourceProxy {}

  protected val out: Outlet[Dataset] = shape.out

  setHandler(out, this)

  def state(): FilterState = FilterState(parameters.uuid, Green)
}

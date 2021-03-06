package io.logbee.keyscore.pipeline.api.stage

import akka.stream._
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.BranchProxy
import io.logbee.keyscore.pipeline.api.{BranchLogic, BranchShape, LogicParameters}

import scala.concurrent.Future

class BranchStage(parameters: LogicParameters, provider:(LogicParameters, BranchShape[Dataset, Dataset, Dataset]) => BranchLogic)
  extends GraphStageWithMaterializedValue[BranchShape[Dataset, Dataset, Dataset], Future[BranchProxy]] {

  private val in = Inlet[Dataset](s"${parameters.uuid}:inlet")
  private val left = Outlet[Dataset](s"${parameters.uuid}:outlet.left")
  private val right = Outlet[Dataset](s"${parameters.uuid}:outlet.right")

  override def shape: BranchShape[Dataset, Dataset,Dataset] = BranchShape(in, left, right)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[BranchProxy]) = {
    val logic = provider(parameters, shape)
    (logic, logic.initPromise.future)
  }
}
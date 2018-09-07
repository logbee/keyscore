package io.logbee.keyscore.agent.pipeline.stage

import akka.stream._
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.{BranchProxy, LogicProxy}

import scala.concurrent.Future

class BranchStage(parameters: LogicParameters, provider:(LogicParameters, BranchShape[Dataset, Dataset, Dataset]) => BranchLogic)
  extends GraphStageWithMaterializedValue[BranchShape[Dataset, Dataset, Dataset], Future[LogicProxy]] {

  private val in = Inlet[Dataset](s"${parameters.uuid}:inlet")
  private val left = Outlet[Dataset](s"${parameters.uuid}:outlet")
  private val right = Outlet[Dataset](s"${parameters.uuid}:outlet")

  override def shape: BranchShape[Dataset, Dataset,Dataset] = BranchShape(in, left, right)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[BranchProxy]) = {
    val logic = provider(parameters, shape)
    (logic, logic.initPromise.future)
  }
}
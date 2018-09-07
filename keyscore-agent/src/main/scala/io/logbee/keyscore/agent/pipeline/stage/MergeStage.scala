package io.logbee.keyscore.agent.pipeline.stage

import akka.stream._
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.MergeProxy

import scala.concurrent.Future

class MergeStage(parameters: LogicParameters, provider:(LogicParameters, MergeShape[Dataset, Dataset, Dataset]) => MergeLogic)
  extends GraphStageWithMaterializedValue[MergeShape[Dataset, Dataset, Dataset], Future[MergeProxy]] {

  private val left = Inlet[Dataset](s"${parameters.uuid}:inlet.left")
  private val right = Inlet[Dataset](s"${parameters.uuid}:inlet.right")
  private val out = Outlet[Dataset](s"${parameters.uuid}:outlet")

  override def shape: MergeShape[Dataset, Dataset,Dataset] = MergeShape(left, right, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[MergeProxy]) = {
    val logic = provider(parameters, shape)
    (logic, logic.initPromise.future)
  }
}
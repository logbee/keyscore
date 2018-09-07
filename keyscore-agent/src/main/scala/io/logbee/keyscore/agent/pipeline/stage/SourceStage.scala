package io.logbee.keyscore.agent.pipeline.stage

import akka.stream._
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.SourceProxy

import scala.concurrent.Future

class SourceStage(parameters: LogicParameters, provider: (LogicParameters, SourceShape[Dataset]) => SourceLogic)
  extends GraphStageWithMaterializedValue[SourceShape[Dataset], Future[SourceProxy]] {

  private val out = Outlet[Dataset](s"${parameters.uuid}:outlet")

  override def shape: SourceShape[Dataset] = SourceShape(out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[SourceProxy]) = {
    val logic = provider(parameters, shape)
    (logic, logic.initPromise.future)
  }
}
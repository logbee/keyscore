package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.FilterProxy

import scala.concurrent.Future

class FilterStage(parameters: LogicParameters, provider:(LogicParameters, FlowShape[Dataset,Dataset]) => FilterLogic)
  extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[FilterProxy]] {

  private val in = Inlet[Dataset](s"${parameters.uuid}:inlet")
  private val out = Outlet[Dataset](s"${parameters.uuid}:outlet")

  override def shape:FlowShape[Dataset,Dataset] = FlowShape(in,out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[FilterProxy]) = {
    val logic = provider(parameters, shape)
    (logic,logic.initPromise.future)
  }
}
package io.logbee.keyscore.agent.stream.stage

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterProxy}

import scala.concurrent.Future

class FilterStage(context:StageContext, configuration:FilterConfiguration, provider:(StageContext, FilterConfiguration, FlowShape[Dataset,Dataset]) => FilterLogic) extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[FilterProxy]] {
  private val in = Inlet[Dataset](s"${configuration.id}:inlet")
  private val out = Outlet[Dataset](s"${configuration.id}:outlet")

  override def shape:FlowShape[Dataset,Dataset] = FlowShape(in,out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[FilterProxy]) = {
    val logic = provider(context,configuration,shape)
    (logic,logic.initPromise.future)
  }
}
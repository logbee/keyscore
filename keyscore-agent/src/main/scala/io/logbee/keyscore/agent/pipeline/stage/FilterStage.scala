package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.filter.FilterProxy

import scala.concurrent.Future

class FilterStage(context:StageContext, configuration:Configuration, provider:(StageContext, Configuration, FlowShape[Dataset,Dataset]) => FilterLogic) extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[FilterProxy]] {

  // TODO: Fix Inlet and Outlet name.
  private val in = Inlet[Dataset](s"${configuration}:inlet")
  private val out = Outlet[Dataset](s"${configuration}:outlet")

  override def shape:FlowShape[Dataset,Dataset] = FlowShape(in,out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[FilterProxy]) = {
    val logic = provider(context, configuration, shape)
    (logic,logic.initPromise.future)
  }
}
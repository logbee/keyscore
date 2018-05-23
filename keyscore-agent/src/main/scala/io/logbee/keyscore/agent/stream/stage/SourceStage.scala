package io.logbee.keyscore.agent.stream.stage

import akka.stream._
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.source.Source

import scala.concurrent.Future

class SourceStage(context: StageContext,configuration:FilterConfiguration,provider: (StageContext, FilterConfiguration, SourceShape[Dataset]) => SourceLogic) extends GraphStageWithMaterializedValue[SourceShape[Dataset], Future[Source]] {

  private val out = Outlet[Dataset](s"${configuration.id}:outlet")

  override def shape: SourceShape[Dataset] = SourceShape(out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Source]) = {

    val logic = provider(context, configuration, shape)
    (logic, logic.initPromise.future)
  }
}
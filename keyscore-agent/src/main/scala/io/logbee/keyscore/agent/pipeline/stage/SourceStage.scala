package io.logbee.keyscore.agent.pipeline.stage

import akka.stream._
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.Future

class SourceStage(context: StageContext,configuration:Configuration,provider: (StageContext, Configuration, SourceShape[Dataset]) => SourceLogic) extends GraphStageWithMaterializedValue[SourceShape[Dataset], Future[SourceProxy]] {

  // TODO: Fix Outlet name.
  private val out = Outlet[Dataset](s"${configuration}:outlet")

  override def shape: SourceShape[Dataset] = SourceShape(out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[SourceProxy]) = {

    val logic = provider(context, configuration, shape)
    (logic, logic.initPromise.future)
  }
}
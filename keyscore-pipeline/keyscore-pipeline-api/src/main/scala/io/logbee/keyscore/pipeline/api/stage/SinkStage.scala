package io.logbee.keyscore.pipeline.api.stage

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import akka.stream.{Attributes, Inlet, SinkShape}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.SinkProxy
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}

import scala.concurrent.Future

class SinkStage(parameters: LogicParameters, provider: (LogicParameters, SinkShape[Dataset]) => SinkLogic)
  extends GraphStageWithMaterializedValue[SinkShape[Dataset], Future[SinkProxy]] {

  private val in = Inlet[Dataset](s"${parameters.uuid}:inlet")

  override def shape: SinkShape[Dataset] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[SinkProxy]) = {
    val logic = provider(parameters, shape)
    (logic, logic.initPromise.future)
  }
}
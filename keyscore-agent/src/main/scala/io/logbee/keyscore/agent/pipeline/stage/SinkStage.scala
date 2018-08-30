package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import akka.stream.{Attributes, Inlet, SinkShape}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.sink.SinkProxy

import scala.concurrent.Future

class SinkStage(context: StageContext, configuration: Configuration, provider: (StageContext, Configuration, SinkShape[Dataset]) => SinkLogic) extends GraphStageWithMaterializedValue[SinkShape[Dataset], Future[SinkProxy]] {

  // TODO: Fix Inlet name.
  private val in = Inlet[Dataset](s"${configuration}:inlet")

  override def shape: SinkShape[Dataset] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[SinkProxy]) = {

    val logic = provider(context, configuration, shape)
    (logic, logic.initPromise.future)
  }
}
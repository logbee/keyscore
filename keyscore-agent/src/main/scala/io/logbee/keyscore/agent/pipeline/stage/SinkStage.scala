package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue}
import akka.stream.{Attributes, Inlet, SinkShape}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.sink.SinkProxy

import scala.concurrent.Future

class SinkStage(context: StageContext, configuration: FilterConfiguration, provider: (StageContext, FilterConfiguration, SinkShape[Dataset]) => SinkLogic) extends GraphStageWithMaterializedValue[SinkShape[Dataset], Future[SinkProxy]] {

  private val in = Inlet[Dataset](s"${configuration.id}:inlet")

  override def shape: SinkShape[Dataset] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[SinkProxy]) = {

    val logic = provider(context, configuration, shape)
    (logic, logic.initPromise.future)
  }
}
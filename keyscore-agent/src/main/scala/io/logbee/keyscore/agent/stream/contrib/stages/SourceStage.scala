package io.logbee.keyscore.agent.stream.contrib.stages

import akka.stream.SourceShape
import akka.stream.stage.GraphStageWithMaterializedValue
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.source.Source

import scala.concurrent.Future

abstract class SourceStage extends GraphStageWithMaterializedValue[SourceShape[Dataset], Future[Source]] {

}
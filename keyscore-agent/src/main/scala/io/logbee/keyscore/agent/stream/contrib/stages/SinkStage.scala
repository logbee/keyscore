package io.logbee.keyscore.agent.stream.contrib.stages

import akka.stream.SinkShape
import akka.stream.stage.GraphStageWithMaterializedValue
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.sink.Sink

import scala.concurrent.Future

abstract class SinkStage extends GraphStageWithMaterializedValue[SinkShape[Dataset], Future[Sink]] {

}

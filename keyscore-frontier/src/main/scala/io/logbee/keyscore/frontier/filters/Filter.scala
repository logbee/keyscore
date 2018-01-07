package io.logbee.keyscore.frontier.filters

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.stage.{GraphStage, GraphStageWithMaterializedValue}

import scala.concurrent.Future

abstract class Filter extends GraphStageWithMaterializedValue[FlowShape[CommittableEvent, CommittableEvent], Future[FilterHandle]]

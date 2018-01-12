package io.logbee.keyscore.frontier.filters

import akka.stream.FlowShape
import akka.stream.stage.GraphStageWithMaterializedValue

import scala.concurrent.Future

abstract class Filter extends GraphStageWithMaterializedValue[FlowShape[CommittableEvent, CommittableEvent], Future[FilterHandle]]

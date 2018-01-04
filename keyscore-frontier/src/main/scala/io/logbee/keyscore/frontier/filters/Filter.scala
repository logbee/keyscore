package io.logbee.keyscore.frontier.filters

import akka.stream.FlowShape
import akka.stream.stage.GraphStage

abstract class Filter extends GraphStage[FlowShape[CommittableEvent, CommittableEvent]]

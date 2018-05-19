package io.logbee.keyscore.agent.stream.stage

import akka.stream.FlowShape
import akka.stream.stage.GraphStageWithMaterializedValue
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.Filter

import scala.concurrent.Future

abstract class FilterStage extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[Filter]] {

}
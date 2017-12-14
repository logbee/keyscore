package filter

import akka.stream.FlowShape
import akka.stream.stage.GraphStage

abstract class Filter extends GraphStage[FlowShape[CommitableFilterMessage,CommitableFilterMessage]]

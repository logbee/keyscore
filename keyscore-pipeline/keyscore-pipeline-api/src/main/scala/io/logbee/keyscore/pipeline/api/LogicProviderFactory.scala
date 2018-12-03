package io.logbee.keyscore.pipeline.api

import java.lang.reflect.Constructor

import akka.stream.{FlowShape, SinkShape, SourceShape}
import io.logbee.keyscore.model.data.Dataset

object LogicProviderFactory {

  def createSinkLogicProvider(logicClass: Class[_]): (LogicParameters, SinkShape[Dataset]) => SinkLogic = {
    val constructor = getLogicConstructor(logicClass)
    (parameters: LogicParameters, shape: SinkShape[Dataset]) => {
      constructor.newInstance(parameters, shape).asInstanceOf[SinkLogic]
    }
  }

  def createSourceLogicProvider(logicClass: Class[_]): (LogicParameters, SourceShape[Dataset]) => SourceLogic = {
    val constructor = getLogicConstructor(logicClass)
    (parameters: LogicParameters, shape: SourceShape[Dataset]) => {
      constructor.newInstance(parameters, shape).asInstanceOf[SourceLogic]
    }
  }

  def createFilterLogicProvider(logicClass: Class[_]): (LogicParameters, FlowShape[Dataset, Dataset]) => FilterLogic = {
    val constructor = getLogicConstructor(logicClass)
    (parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) => {
      constructor.newInstance(parameters, shape).asInstanceOf[FilterLogic]
    }
  }

  def createBranchLogicProvider(logicClass: Class[_]): (LogicParameters, BranchShape[Dataset, Dataset, Dataset]) => BranchLogic = {
    val constructor = getLogicConstructor(logicClass)
    (parameters: LogicParameters, shape: BranchShape[Dataset, Dataset, Dataset]) => {
      constructor.newInstance(parameters, shape).asInstanceOf[BranchLogic]
    }
  }

  def createMergeLogicProvider(logicClass: Class[_]): (LogicParameters, MergeShape[Dataset, Dataset, Dataset]) => MergeLogic = {
    val constructor = getLogicConstructor(logicClass)
    (parameters: LogicParameters, shape: MergeShape[Dataset, Dataset, Dataset]) => {
      constructor.newInstance(parameters, shape).asInstanceOf[MergeLogic]
    }
  }

  private def getLogicConstructor(logicClass: Class[_]): Constructor[_] = {
    logicClass.getConstructors()(0)
  }
}

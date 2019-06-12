package io.logbee.keyscore.pipeline.contrib.test

import java.util.UUID
import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.LogicProviderFactory.{createFilterLogicProvider, createSinkLogicProvider, createSourceLogicProvider}
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, SinkStage, SourceStage, StageContext}
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters, SinkLogic, SourceLogic}

import scala.concurrent.ExecutionContextExecutor
import scala.reflect.runtime.universe._

object TestStreamFor {
  def resolveClass[T](implicit tag: TypeTag[T]): Class[_] = {
    val mirror = runtimeMirror(getClass.getClassLoader)
    mirror.runtimeClass(typeOf[T].typeSymbol.asClass)
  }
}

class TestStreamForFilter[T <: FilterLogic](configuration: Configuration = Configuration())(implicit system: ActorSystem, executionContext: ExecutionContextExecutor, materializer: Materializer, tag: TypeTag[T]) {

  private val context = StageContext(system, executionContext)
  private val filterStage = new FilterStage(LogicParameters(randomUUID(), StageSupervisor.noop, context, configuration), createFilterLogicProvider(TestStreamFor.resolveClass[T](tag)))

  val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
    .viaMat(filterStage)(Keep.both)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()
}

class TestStreamForSource[T <: SourceLogic](configuration: Configuration = Configuration())(implicit system: ActorSystem, executionContext: ExecutionContextExecutor, materializer: Materializer, tag: TypeTag[T]) {

  val context = StageContext(system, executionContext)
  val sourceStage = new SourceStage(LogicParameters(UUID.randomUUID(), StageSupervisor.noop, context, configuration), createSourceLogicProvider(TestStreamFor.resolveClass[T](tag)))

  val (sourceFuture, sink) = Source.fromGraph(sourceStage)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()
}

class TestStreamForSink[T <: SinkLogic](configuration: Configuration = Configuration())(implicit system: ActorSystem, executionContext: ExecutionContextExecutor, materializer: Materializer, tag: TypeTag[T]) {

  val context = StageContext(system, executionContext)
  val sinkStage = new SinkStage(LogicParameters(UUID.randomUUID(), StageSupervisor.noop, context, configuration), createSinkLogicProvider(TestStreamFor.resolveClass[T](tag)))

  val (source, sinkFuture) = TestSource.probe[Dataset]
    .toMat(sinkStage)(Keep.both)
    .run()
}
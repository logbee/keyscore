package io.logbee.keyscore.pipeline.contrib.test

import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.pipeline.api.LogicProviderFactory.createFilterLogicProvider
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}

import scala.concurrent.ExecutionContextExecutor

class TestStreamWithSourceAndSink[T <: FilterLogic](configuration: Configuration, logicClass: Class[T])(implicit system: ActorSystem, executionContext: ExecutionContextExecutor, materializer: Materializer) {
  val context = StageContext(system, executionContext)
  val filterStage = new FilterStage(LogicParameters(randomUUID(), context, configuration), createFilterLogicProvider(logicClass))
  val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
    .viaMat(filterStage)(Keep.both)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()
}

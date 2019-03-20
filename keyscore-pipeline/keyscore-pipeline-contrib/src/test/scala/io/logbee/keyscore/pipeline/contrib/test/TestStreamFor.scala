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
import scala.reflect.runtime.universe._

class TestStreamFor[T <: FilterLogic](configuration: Configuration = Configuration())(implicit system: ActorSystem, executionContext: ExecutionContextExecutor, materializer: Materializer, tag: TypeTag[T]) {

  private val context = StageContext(system, executionContext)
  private val filterStage = new FilterStage(LogicParameters(randomUUID(), context, configuration), createFilterLogicProvider(resolveClass(tag)))

  val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
    .viaMat(filterStage)(Keep.both)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()

  private def resolveClass(implicit tag: TypeTag[T]) = {
    val mirror = runtimeMirror(getClass.getClassLoader)
    mirror.runtimeClass(typeOf[T].typeSymbol.asClass)
  }
}

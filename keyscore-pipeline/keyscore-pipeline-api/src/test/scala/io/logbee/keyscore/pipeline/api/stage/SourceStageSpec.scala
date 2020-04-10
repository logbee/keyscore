package io.logbee.keyscore.pipeline.api.stage

import java.util.UUID

import akka.stream.SourceShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Promise
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class SourceStageSpec extends AnyFreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A SourceStage" - {

    "should pass the appropriate configuration to its logic" in {

      val updateConfiguration = Promise[Configuration]
      val initializeConfiguration = Promise[Configuration]

      val configurationA = Configuration()
      val configurationB = Configuration()
      val context = StageContext(system, executionContext)

      val provider = (parameters: LogicParameters, s: SourceShape[Dataset]) => new SourceLogic(parameters, s) {

        override def initialize(configuration: Configuration): Unit = {
          initializeConfiguration.success(configuration)
        }

        override def configure(configuration: Configuration): Unit = {
          updateConfiguration.success(configuration)
        }

        override def onPull(): Unit = ???
      }

      val sourceFuture = Source.fromGraph(new SourceStage(LogicParameters(UUID.randomUUID(), StageSupervisor.noop, context, configurationA), provider))
        .toMat(TestSink.probe[Dataset])(Keep.left)
        .run()

      whenReady(sourceFuture) { source =>
        source.configure(configurationB)
      }

      whenReady(initializeConfiguration.future) { success =>
        success shouldBe configurationA
      }

      whenReady(updateConfiguration.future) { success =>
        success shouldBe configurationB
      }
    }
  }
}

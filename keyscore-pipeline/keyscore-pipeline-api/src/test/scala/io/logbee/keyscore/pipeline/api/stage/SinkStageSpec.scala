package io.logbee.keyscore.pipeline.api.stage

import java.util.UUID

import akka.stream.SinkShape
import akka.stream.scaladsl.{Keep, Source}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}
import io.logbee.keyscore.test.fixtures.ExampleData._
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Promise
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class SinkStageSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A sink stage" - {

    "should pass the appropriate configuration to its logic" in {

      val updateConfiguration = Promise[Configuration]
      val initializeConfiguration = Promise[Configuration]

      val configurationA = Configuration()
      val configurationB =  Configuration()
      val context: StageContext = StageContext(system, executionContext)

      val provider = (parameters: LogicParameters, s: SinkShape[Dataset]) => new SinkLogic(parameters, s) {

        override def initialize(configuration: Configuration): Unit = {
          initializeConfiguration.success(configuration)
        }

        override def configure(configuration: Configuration): Unit = {
          updateConfiguration.success(configuration)
        }

        override def onPush(): Unit = {

        }
      }

      val sinkFuture = Source(List(dataset1, dataset2))
        .toMat(new SinkStage(LogicParameters(UUID.randomUUID(), StageSupervisor.noop, context, configurationA), provider))(Keep.right)
        .run()

      whenReady(sinkFuture) { sink =>
        sink.configure(configurationB)
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

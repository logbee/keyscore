package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.testkit.TestProbe
import akka.util.Timeout
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.test.fixtures.ProductionSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class LocalPipelineManagerSpec extends AnyFreeSpecLike with ProductionSystemWithMaterializerAndExecutionContext {

  implicit val timeout: Timeout = 30 seconds

  val streamId = UUID.randomUUID()
  val sourceConfiguration = Configuration()
  val sinkConfiguration = Configuration()

  "A LocalPipelineManager " - {

    "start a StreamSupervisor for a pipeline" in {

      val filterManagerProbe = TestProbe("filter-manager")
      val testee = system.actorOf(LocalPipelineManager(filterManagerProbe.ref), "LocalPipelineManager")

      val streamConfiguration = PipelineConfiguration(streamId, "test", "A test pipeline.", sourceConfiguration, List.empty, sinkConfiguration)

//      testee ! CreatePipeline(streamConfiguration)

//      whenReady(().mapTo[ActorRef]) { ref =>
//        whenReady((ref ? RequestPipelineInstance).mapTo[PipelineInstance]) { state =>
//          state shouldBe PipelineInstance(streamId, Health.Green, streamConfiguration)
//        }
//
//        testee ! UpdatePipeline(streamConfiguration)
//      }
    }
  }
}

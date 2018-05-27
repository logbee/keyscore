package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.testkit.TestProbe
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.PipelineManager.CreatePipeline
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class PipelineManagerSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  implicit val timeout: Timeout = 30 seconds

  val streamId = UUID.randomUUID()
  val sourceConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-source"))
  val sinkConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-sink"))

  "A PipelineManager " should {

    "start a StreamSupervisor for a pipeline" in {

      val filterManagerProbe = TestProbe("filter-manager")
      val testee = system.actorOf(PipelineManager(filterManagerProbe.ref), "pipeline-manager")

      val streamConfiguration = PipelineConfiguration(streamId, "test", "A test pipeline.", sourceConfiguration, List.empty, sinkConfiguration)

      testee ! CreatePipeline(streamConfiguration)

//      whenReady(().mapTo[ActorRef]) { ref =>
//        whenReady((ref ? RequestPipelineState).mapTo[PipelineState]) { state =>
//          state shouldBe PipelineState(streamId, Health.Green, streamConfiguration)
//        }
//
//        testee ! UpdatePipeline(streamConfiguration)
//      }
    }
  }
}

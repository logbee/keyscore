package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.testkit.TestProbe
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.PipelineScheduler.UpdatedConfiguration
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor.CreatePipeline
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
class PipelineSchedulerSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  implicit val timeout: Timeout = 30 seconds

  val pipelineId = UUID.randomUUID()
  val sourceConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-source"))
  val sinkConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-sink"))
  val agentProbe = TestProbe("agentProbe")
  "A PipelineScheduler " should {

    "start a PipelineSupervisor for a pipeline" in {

      val filterManagerProbe = TestProbe("filter-manager")
      val scheduler = system.actorOf(PipelineScheduler(filterManagerProbe.ref), "PipelineScheduler")

      val pipelineConfiguration = PipelineConfiguration(pipelineId, "test", "A test pipeline.", sourceConfiguration, List.empty, sinkConfiguration)

      scheduler tell(CreatePipeline(pipelineConfiguration), agentProbe.ref)
      agentProbe.expectMsg(UpdatedConfiguration(pipelineConfiguration))
      


    }
  }
}

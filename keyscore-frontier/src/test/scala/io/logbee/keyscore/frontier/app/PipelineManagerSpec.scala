package io.logbee.keyscore.frontier.app

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import io.logbee.keyscore.frontier.cluster.PipelineManager
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpecLike, Matchers}

@RunWith(classOf[JUnitRunner])
class PipelineManagerSpec extends TestKit(ActorSystem("actorSystem")) with FreeSpecLike with Matchers with ScalaFutures with MockFactory {

  trait TestSetup {

    val sinkId = UUID.randomUUID()
    val sourceId = UUID.randomUUID()
    val agentManager = TestProbe("agent-manager")
    val agent1 = TestProbe("agent1")
    val agent2 = TestProbe("agent2")
    val scheduler2 = TestProbe("scheduler1")

    val pipelineManager = system.actorOf(PipelineManager(agentManager.ref, (_, context) => {
      context.actorSelection(scheduler2.ref.path)
    }))

//    val sourceConfiguration = FilterConfiguration(FilterDescriptor(sourceId, "test-source"))
//    val sinkConfiguration = FilterConfiguration(FilterDescriptor(sinkId, "test-sink"))
//    val pipelineConfiguration = PipelineConfiguration(streamId, "test-configuration", "A test pipeline", sourceConfiguration, List.empty, sinkConfiguration)
//
//    val metaFilterDescriptorSource =
//      MetaFilterDescriptor(sourceId, "test-source", Map(Locale.ENGLISH -> FilterDescriptorFragment("fragment", "description", FilterConnection(true, List.empty), FilterConnection(false, List.empty))))
//    val metaFilterDescriptorSink =
//      MetaFilterDescriptor(sinkId, "test-sink", Map(Locale.ENGLISH -> FilterDescriptorFragment("fragment", "description", FilterConnection(true, List.empty), FilterConnection(false, List.empty))))
  }

  val streamId = UUID.randomUUID()

  "A PipelineManager" - {

    "should send CreatePipelineOrder() when an agent is available" in new TestSetup {

//      pipelineManager.tell(AgentCapabilities(List(metaFilterDescriptorSink, metaFilterDescriptorSource)), agent2.ref)
//
//      pipelineManager ! CreatePipeline(pipelineConfiguration)
//      scheduler2.expectMsg(CreatePipelineOrder(pipelineConfiguration))
    }

    "should remove agents from the availableAgents list after he left cluster" in new TestSetup {
//      pipelineManager.tell(AgentCapabilities(List(metaFilterDescriptorSink, metaFilterDescriptorSource)), agent1.ref)
//      pipelineManager.tell(AgentCapabilities(List(metaFilterDescriptorSink, metaFilterDescriptorSource)), agent2.ref)
//
//      pipelineManager ! AgentLeaved(agent1.ref)
//      pipelineManager ! CreatePipeline(pipelineConfiguration)
//
//      scheduler2.expectMsg(CreatePipelineOrder(pipelineConfiguration))
    }
  }
}

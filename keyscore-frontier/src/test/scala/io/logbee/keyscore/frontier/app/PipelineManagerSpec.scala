package io.logbee.keyscore.frontier.app

import java.util.{Locale, UUID}

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import io.logbee.keyscore.commons.cluster.{AgentCapabilities, AgentLeaved, CreatePipelineOrder}
import io.logbee.keyscore.frontier.app.PipelineManager.CreatePipeline
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.filter._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class PipelineManagerSpec extends TestKit(ActorSystem("actorSystem")) with  WordSpecLike with Matchers with ScalaFutures with MockFactory  {

  trait TestSetup {

    val sinkUuid = UUID.randomUUID()
    val sourceUuid = UUID.randomUUID()
    val agentManager = TestProbe("agent-manager")
    val pipelineManager = system.actorOf(PipelineManager(agentManager.ref))
    val agent1 = TestProbe("agent1")
    val agent2 = TestProbe("agent2")


    val sourceConfiguration = FilterConfiguration(FilterDescriptor(sourceUuid, "test-source"))
    val sinkConfiguration = FilterConfiguration(FilterDescriptor(sinkUuid, "test-sink"))
    val pipelineConfiguration = PipelineConfiguration(streamId,"test-configuration","A test pipeline", sourceConfiguration, List.empty, sinkConfiguration)

    val metaFilterDescriptorSource =
      MetaFilterDescriptor(sourceUuid,"test-source",Map(Locale.ENGLISH -> FilterDescriptorFragment("fragment", "description", FilterConnection(true, List.empty),FilterConnection(false,List.empty))))
    val metaFilterDescriptorSink =
      MetaFilterDescriptor(sinkUuid,"test-sink",Map(Locale.ENGLISH -> FilterDescriptorFragment("fragment", "description", FilterConnection(true, List.empty),FilterConnection(false,List.empty))))
  }

  val streamId = UUID.randomUUID()




  "A PipelineManager should" should {

    "Should send CreatePipelineOrder() when an agent is available" in new TestSetup {

      pipelineManager.tell(AgentCapabilities(List(metaFilterDescriptorSink,metaFilterDescriptorSource)),agent1.ref)

      pipelineManager ! CreatePipeline(pipelineConfiguration)
      agent1.expectMsg(CreatePipelineOrder(pipelineConfiguration))
    }

    "Check if Agent is removed from availableAgents list after he left cluster" in new TestSetup {
      pipelineManager.tell(AgentCapabilities(List(metaFilterDescriptorSink, metaFilterDescriptorSource)),agent1.ref)
      pipelineManager.tell(AgentCapabilities(List(metaFilterDescriptorSink, metaFilterDescriptorSource)),agent2.ref)

      pipelineManager ! AgentLeaved(agent1.ref)
      pipelineManager ! CreatePipeline(pipelineConfiguration)

      agent2.expectMsg(CreatePipelineOrder(pipelineConfiguration))
    }
  }

}

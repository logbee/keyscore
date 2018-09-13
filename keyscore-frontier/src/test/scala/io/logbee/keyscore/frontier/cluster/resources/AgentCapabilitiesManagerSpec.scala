package io.logbee.keyscore.frontier.cluster.resources

import akka.actor.ActorRef
import akka.testkit.TestProbe
import io.logbee.keyscore.commons.cluster.{AgentCapabilities, AgentLeaved}
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages.StoreDescriptorRequest
import io.logbee.keyscore.commons.test.ProductionSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.commons.{DescriptorService, HereIam}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentCapabilitiesManager
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentCapabilitiesManager.{AgentsForPipelineRequest, AgentsForPipelineResponse, GetDescriptors, GetDescriptorsResponse}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import org.scalatest.{Matchers, WordSpecLike}

import scala.collection.mutable

class AgentCapabilitiesManagerSpec extends ProductionSystemWithMaterializerAndExecutionContext with WordSpecLike with Matchers {

  trait TestSetup {

    val descriptorManagerProbe = TestProbe("descriptor-manager")
    val someActor = TestProbe("some-actor")
    val agent1 = TestProbe("agent1")
    val agent2 = TestProbe("agent2")
    var availableAgents1: mutable.Map[ActorRef, Seq[Descriptor]] = mutable.Map.empty[ActorRef, Seq[Descriptor]]
    var availableAgents2: mutable.Map[ActorRef, Seq[Descriptor]] = mutable.Map.empty[ActorRef, Seq[Descriptor]]

    val descriptor1 = Descriptor(DescriptorRef("b707d43b-e2fe-4d9e-bea5-3a326b1e2aba"))
    val descriptor2 = Descriptor(DescriptorRef("2be45b6d-8193-40a2-82ba-e54ccfe0a9d2"))
    val descriptor3 = Descriptor(DescriptorRef("1f0da086-8e70-4159-acd5-d38fd8be7639"))
    val descriptor4 = Descriptor(DescriptorRef("b4683926-f068-4f6d-b3a4-802e67663043"))

    val descriptors1 = Seq(descriptor1, descriptor2)
    availableAgents1.put(agent1.ref, descriptors1)
    val descriptors2 = Seq(descriptor3, descriptor4)
    availableAgents2.put(agent1.ref, descriptors2)

    val agentCapabilitiesManager = system.actorOf(AgentCapabilitiesManager())
    agentCapabilitiesManager ! HereIam(DescriptorService, descriptorManagerProbe.ref)

    val responseMap: mutable.Map[DescriptorRef, Descriptor] = mutable.Map.empty[DescriptorRef, Descriptor]
    responseMap.put(descriptor1.ref, descriptor1)
    responseMap.put(descriptor2.ref, descriptor2)
    responseMap.put(descriptor3.ref, descriptor3)
    responseMap.put(descriptor4.ref, descriptor4)

  }
    "An AgentCapabilitiesManager" should {

      "add descriptors to it's lists when an agent publishes his capabilities" in new TestSetup {
        agentCapabilitiesManager tell(AgentCapabilities(descriptors1.toList), agent1.ref)
        agentCapabilitiesManager tell(AgentCapabilities(descriptors2.toList), agent2.ref)

        descriptorManagerProbe.expectMsg(StoreDescriptorRequest(descriptor1))
        descriptorManagerProbe.expectMsg(StoreDescriptorRequest(descriptor2))

        descriptorManagerProbe.expectMsg(StoreDescriptorRequest(descriptor3))
        descriptorManagerProbe.expectMsg(StoreDescriptorRequest(descriptor4))
      }

      "retrieve all StandardDescriptors" in new TestSetup {
        agentCapabilitiesManager tell(AgentCapabilities(descriptors1.toList), agent1.ref)
        agentCapabilitiesManager tell(AgentCapabilities(descriptors2.toList), agent2.ref)

        agentCapabilitiesManager tell (GetDescriptors, someActor.ref)
        val descriptorResponseMessage = someActor.expectMsgType[GetDescriptorsResponse]

       descriptorResponseMessage.listOfDescriptorsAndType should have size 4
       descriptorResponseMessage.listOfDescriptorsAndType should contain theSameElementsAs (descriptors1 ++ descriptors2)
      }

      "retrieve all the agents that can build the requested pipeline" in new TestSetup {
        agentCapabilitiesManager tell(AgentCapabilities(descriptors1.toList), agent1.ref)
        agentCapabilitiesManager tell(AgentCapabilities(descriptors2.toList), agent2.ref)

        agentCapabilitiesManager tell (AgentsForPipelineRequest(descriptors1.map(desc => desc.ref).toList), someActor.ref)
        someActor.expectMsg(AgentsForPipelineResponse(List(agent1.ref)))
      }

      "remove an agent from the available agents list" in new TestSetup {
        agentCapabilitiesManager tell(AgentCapabilities(descriptors1.toList), agent1.ref)
        agentCapabilitiesManager tell(AgentCapabilities(descriptors2.toList), agent2.ref)

        agentCapabilitiesManager tell (AgentLeaved(agent1.ref), someActor.ref)
        agentCapabilitiesManager tell (AgentsForPipelineRequest(descriptors1.map(desc => desc.ref).toList), someActor.ref)
        val agentsForPipelineResponseMessage = someActor.expectMsgType[AgentsForPipelineResponse]
        agentsForPipelineResponseMessage.possibleAgents should have size 0
      }
    }
}

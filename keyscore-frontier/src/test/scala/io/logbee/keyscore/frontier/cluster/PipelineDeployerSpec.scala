package io.logbee.keyscore.frontier.cluster

import akka.actor.{ActorContext, ActorRef, ActorSelection}
import akka.testkit.{TestActor, TestProbe}
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetBlueprintRequest, GetBlueprintResponse, GetPipelineBlueprintRequest, GetPipelineBlueprintResponse}
import io.logbee.keyscore.commons.test.{ProductionSystemWithMaterializerAndExecutionContext, TestSystemWithMaterializerAndExecutionContext}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentCapabilitiesManager.{AgentsForPipelineRequest, AgentsForPipelineResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentStatsManager.{AgentStats, StatsForAgentsRequest, StatsForAgentsResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.supervisor.PipelineDeployer
import io.logbee.keyscore.frontier.cluster.pipeline.supervisor.PipelineDeployer.{CreatePipelineRequest, PipelineDeployed}
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SourceBlueprint}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

class PipelineDeployerSpec extends ProductionSystemWithMaterializerAndExecutionContext with WordSpecLike with Matchers with BeforeAndAfter {

trait TestSetup {
  val someActor = TestProbe("someActor")
  val blueprintManagerProbe = TestProbe("blueprint-manager")
  val agentStatsManagerProbe = TestProbe("agentstats-manager")
  val agentCapabilitiesManagerProbe = TestProbe("agentcapabilities-manager")
  val localPipelineManagerProbe = TestProbe("localpipeline-manager")
  val agent1 = TestProbe("firstAgent")
  val agent2 = TestProbe("agentSmith")

  val sourceBlueprintRef = BlueprintRef("fd8003d2-9c37-41fb-be4b-cafe91c6e0db")
  val sourceBlueprint = SourceBlueprint(sourceBlueprintRef)
  val pipelineBlueprint = PipelineBlueprint(BlueprintRef("b7458edb-9132-4694-9a74-8ed760747bb7"), Seq(sourceBlueprint))

  var localPipelineManager: (ActorRef, ActorContext) => ActorSelection = (_, _) => system.actorSelection(localPipelineManagerProbe.ref.path.toSerializationFormat)
  val pipelineDeployer = system.actorOf(PipelineDeployer(localPipelineManager))
}
  "A PipelineDeployer" should {

    "resolve a pipelineBlueprintRef" in new TestSetup {
      val blueprint = PipelineBlueprint(BlueprintRef("a3f17a75-595b-4325-9efb-8be43bf47d0c"))

      blueprintManagerProbe.setAutoPilot((sender: ActorRef, message: Any) => message match {
        case _: GetPipelineBlueprintRequest =>
          println(" # # # # # # GetPipelineBlueprintRequest # # # # #")
          sender ! GetPipelineBlueprintResponse(Option(pipelineBlueprint))
          TestActor.KeepRunning

        case _: GetBlueprintRequest =>
          sender ! GetBlueprintResponse(Option(sourceBlueprint))
          TestActor.KeepRunning

        case _ =>
          TestActor.KeepRunning
      })
      agentCapabilitiesManagerProbe.setAutoPilot((sender: ActorRef, message: Any) => message match {
        case _: AgentsForPipelineRequest =>
          sender ! AgentsForPipelineResponse(List(agent1.ref, agent2.ref))
          TestActor.KeepRunning

      })

      agentStatsManagerProbe.setAutoPilot((sender: ActorRef, message: Any) => message match {
        case _: StatsForAgentsRequest =>
          sender ! StatsForAgentsResponse(Map(agent1.ref -> AgentStats(0), agent2.ref -> AgentStats(0)))
          TestActor.KeepRunning

      })

      pipelineDeployer ! CreatePipelineRequest(blueprint.ref)

      pipelineDeployer ! HereIam(BlueprintService, blueprintManagerProbe.ref)
      pipelineDeployer ! HereIam(AgentStatsService, agentStatsManagerProbe.ref)
      pipelineDeployer ! HereIam(AgentCapabilitiesService, agentCapabilitiesManagerProbe.ref)


//      someActor.expectMsg(PipelineDeployed)

      localPipelineManagerProbe.expectMsg()
    }




  }
}

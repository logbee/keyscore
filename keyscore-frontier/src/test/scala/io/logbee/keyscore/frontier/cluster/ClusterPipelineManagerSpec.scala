package io.logbee.keyscore.frontier.cluster

import java.util.UUID

import akka.testkit.TestProbe
import io.logbee.keyscore.commons.test.ProductionSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.frontier.cluster.pipeline.manager.{AgentStatsManager, ClusterPipelineManager}
import io.logbee.keyscore.model.blueprint.{PipelineBlueprint, SinkBlueprint, SourceBlueprint}
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}
import io.logbee.keyscore.model.descriptor.Descriptor
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpecLike, Matchers}

@RunWith(classOf[JUnitRunner])
class ClusterPipelineManagerSpec extends ProductionSystemWithMaterializerAndExecutionContext with FreeSpecLike with Matchers with ScalaFutures with MockFactory {

  trait TestSetup {

    //TODO

//    val sinkId = UUID.randomUUID()
//    val sourceId = UUID.randomUUID()
//    val clusterAgentManager = TestProbe("cluster-agent-manager")
//    val agent1 = TestProbe("agent1")
//    val agent2 = TestProbe("agent2")
//    val scheduler = TestProbe("scheduler")
//
//    val clusterPipelineManager = system.actorOf(ClusterPipelineManager(clusterAgentManager.ref, (_, context) => {
//      context.actorSelection(scheduler.ref.path)
//    }))
//
//    val sourceDescriptor = Descriptor(ref = "392b197a-fbbc-4ba7-8149-cc12aee7c874")
//    val sinkDescriptor = Descriptor(ref = "e1f6c5f9-5d6a-489f-b7e1-68ab2d57ed48")
//
//    val sourceConfiguration = Configuration(ref = ConfigurationRef("385bb18a-e377-4fc4-ab28-8366f603e80e"))
//    val sinkConfiguration = Configuration(ref = ConfigurationRef("94c74e75-ed1c-4ecb-baa2-69dea56f87f5"))
//
//    val pipelineBlueprint = PipelineBlueprint(ref = "53ef3d23-f121-4629-848d-18e1b68a6bad", Seq(
//      SourceBlueprint(ref = "f562ada9-48a6-4602-a05f-dde3638d78dc", sourceDescriptor.ref, sourceConfiguration.ref),
//      SinkBlueprint(ref = "6948e7ad-a8cb-4e84-9621-850d122e265a", sinkDescriptor.ref, sourceConfiguration.ref)
//    ))

  }


  "A ClusterPipelineManager" - {

    "should send CreatePipelineRequest when a request comes in" in new TestSetup {
      // TODO: Enhance me !
      //      pipelineManager.tell(CreatePipeline(pipelineBlueprint), agent1.ref)

    }
  }
}

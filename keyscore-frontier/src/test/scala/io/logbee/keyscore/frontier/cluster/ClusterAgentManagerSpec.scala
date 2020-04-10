package io.logbee.keyscore.frontier.cluster

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.ClusterEvent.{MemberUp, UnreachableMember}
import akka.cluster.MockableMember
import akka.testkit.{TestKit, TestProbe}
import io.logbee.keyscore.commons.cluster.{AgentJoin, AgentJoinAccepted, MemberAdded, MemberRemoved}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterAgentManager
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterAgentManager.{QueryAgents, QueryAgentsResponse, QueryMembers, QueryMembersResponse}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpecLike

class ClusterAgentManagerSpec extends TestKit(ActorSystem("ClusterAgentManagerSpec")) with AnyWordSpecLike with ScalaFutures with BeforeAndAfter {

  var clusterAgentManager: ActorRef = _

  val actorUUID = UUID.randomUUID()


  before {
    clusterAgentManager = system.actorOf(Props[ClusterAgentManager])
  }

  after {
    system.stop(clusterAgentManager)
  }

  "A ClusterAgentManager" should {

    "return an empty list if no agent has joined" in {

      val probe = TestProbe()

      clusterAgentManager tell (QueryAgents, probe.ref)

      probe.expectMsg(QueryAgentsResponse(List.empty))
    }

    "return a list containing joined agents" in {

      val probe = TestProbe()

      clusterAgentManager tell (AgentJoin(actorUUID, "test-agent"), probe.ref)
      clusterAgentManager tell (QueryAgents, probe.ref)

      probe.expectMsgType[AgentJoinAccepted]
      probe.expectMsg(QueryAgentsResponse(List(RemoteAgent(actorUUID, "test-agent", 0, probe.ref))))
    }

    "increase the list of members by one when adding a new agent member with role keyscore-agent" in {

      val probe = TestProbe()

      val mockerMember = new MockableMember(roles = Set("keyscore-agent"))

      clusterAgentManager tell (MemberUp(mockerMember), probe.ref)
      clusterAgentManager tell (QueryMembers, probe.ref)

      probe.expectMsgType[MemberAdded]
      probe.expectMsg(QueryMembersResponse(List(mockerMember)))
    }

    "decrease the list of members by one when removing a member with role keyscore-agent" in {

      val probe = TestProbe()

      val mockerMember = new MockableMember()

      clusterAgentManager tell(UnreachableMember(mockerMember), probe.ref)

      probe.expectMsgType[MemberRemoved]
    }
  }
}

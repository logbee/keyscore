package io.logbee.keyscore.frontier.cluster

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.ClusterEvent.{MemberUp, UnreachableMember}
import akka.cluster.MockableMember
import akka.testkit.{TestKit, TestProbe}
import io.logbee.keyscore.commons.cluster.{AgentJoin, AgentJoinAccepted, MemberAdded, MemberRemoved}
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse, QueryMembers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, WordSpecLike}

import java.util.UUID

class AgentManagerSpec extends TestKit(ActorSystem("AgentManagerSpec")) with WordSpecLike with ScalaFutures with BeforeAndAfter {

  var agentManager: ActorRef = _

  val actorUUID = UUID.randomUUID()


  before {
    agentManager = system.actorOf(Props[AgentManager])
  }

  after {
    system.stop(agentManager)
  }

  "A AgentManager" should {

    "return an empty list if no agent has joined" in {

      val probe = TestProbe()

      agentManager tell (QueryAgents, probe.ref)

      probe.expectMsg(QueryAgentsResponse(List.empty))
    }

    "return a list containing joined agents" in {

      val probe = TestProbe()

      agentManager tell (AgentJoin(actorUUID, "test-agent"), probe.ref)
      agentManager tell (QueryAgents, probe.ref)

      probe.expectMsgType[AgentJoinAccepted]
      probe.expectMsg(QueryAgentsResponse(List(RemoteAgent(actorUUID, "test-agent", 0, probe.ref))))
    }

    "increase the list of members by one when adding a new agent member with role keyscore-agent" in {

      val probe = TestProbe()

      val mockerMember = new MockableMember(roles = Set("keyscore-agent"))

      agentManager tell (MemberUp(mockerMember), probe.ref)
      agentManager tell (QueryMembers, probe.ref)

      probe.expectMsgType[MemberAdded]
      probe.expectMsg(QueryMembers(List(mockerMember)))
    }

    "decrease the list of members by one when removing a member with role keyscore-agent" in {

      val probe = TestProbe()

      val mockerMember = new MockableMember()

      agentManager tell(UnreachableMember(mockerMember), probe.ref)

      probe.expectMsgType[MemberRemoved]
    }
  }
}

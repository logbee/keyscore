package io.logbee.keyscore.frontier.cluster

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import io.logbee.keyscore.commons.cluster.{AgentJoin, AgentJoinAccepted}
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, WordSpecLike}

class AgentManagerSpec extends TestKit(ActorSystem("AgentManagerSpec")) with WordSpecLike with ScalaFutures with BeforeAndAfter {

  var agentManager: ActorRef = _

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

      agentManager tell (AgentJoin("test-agent"), probe.ref)
      agentManager tell (QueryAgents, probe.ref)

      probe.expectMsgType[AgentJoinAccepted]
      probe.expectMsg(QueryAgentsResponse(List(RemoteAgent("test-agent", 0, probe.ref))))
    }
  }
}

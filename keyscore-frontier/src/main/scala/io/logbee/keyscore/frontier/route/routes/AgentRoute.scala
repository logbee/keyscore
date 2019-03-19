package io.logbee.keyscore.frontier.route.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import io.logbee.keyscore.commons.cluster.{AgentRemovedFromCluster, RemoveAgentFromCluster}
import io.logbee.keyscore.frontier.auth.AuthorizationHandler
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterAgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.model.AgentModel

/**
  * The '''AgentRoute''' holds the REST route for all `Agents`.<br><br>
  * `Directives`: GET | DELETE <br>
  * Operations: For a single Agent. <br>
  */
trait AgentRoute extends RouteImplicits with AuthorizationHandler  {

  def agentsRoute(clusterAgentManager: ActorRef): Route = {
    pathPrefix("agent") {
      authorize { token =>
        pathPrefix(JavaUUID) { agentID =>
          delete {
            onSuccess(clusterAgentManager ? RemoveAgentFromCluster(agentID)) {
              case AgentRemovedFromCluster(agentID) => complete(StatusCodes.OK)
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        } ~
          get {
            onSuccess(clusterAgentManager ? QueryAgents) {
              case QueryAgentsResponse(agents) => complete(StatusCodes.OK, agents.map(agent => AgentModel(agent.id.toString, agent.name, agent.ref.path.address.host.get)))
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
      }
    }
  }
}

package io.logbee.keyscore.frontier.route.routes

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.{AgentRemovedFromCluster, RemoveAgentFromCluster}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterAgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.frontier.route.routes.AgentRoute.{AgentRouteRequest, AgentRouteResponse}
import io.logbee.keyscore.model.AgentModel

object AgentRoute {

  case class AgentRouteRequest(agentClusterManager: ActorRef)

  case class AgentRouteResponse(agentRoute: Route)

}

/**
  * The '''AgentRoute''' holds the REST route for all `Agents`.<br><br>
  * `Directives`: GET | DELETE <br>
  * Operations: For a single Agent. <br>
  */
class AgentRoute extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  override def receive: Receive = {
    case AgentRouteRequest(agentClusterManager) =>
      val r = agentsRoute(agentClusterManager)
      sender ! AgentRouteResponse(r)
  }

  def agentsRoute(clusterAgentManager: ActorRef): Route = {
    pathPrefix("agent") {
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

package io.logbee.keyscore.frontier.route.routes

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.{AgentRemovedFromCluster, RemoveAgentFromCluster}
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.route.routes.AgentRoute.{AgentRouteRequest, AgentRouteResponse}
import io.logbee.keyscore.model.AgentModel
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization

object AgentRoute {
  case class AgentRouteRequest(agentManager: ActorRef)
  case class AgentRouteResponse(agentRoute: Route)
}

class AgentRoute extends Actor with ActorLogging with Json4sSupport {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  override def receive: Receive = {
    case AgentRouteRequest(agentManager) =>
      val r = agentsRoute(agentManager)
      sender ! AgentRouteResponse(r)
  }

  def agentsRoute(agentManager: ActorRef): Route = {
    pathPrefix("agent") {
      pathPrefix(JavaUUID) { agentID =>
        delete {
          onSuccess(agentManager ? RemoveAgentFromCluster(agentID)) {
            case AgentRemovedFromCluster(agentID) => complete(StatusCodes.OK)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      } ~
        get {
          onSuccess(agentManager ? QueryAgents) {
            case QueryAgentsResponse(agents) => complete(StatusCodes.OK, agents.map(agent => AgentModel(agent.id.toString, agent.name, agent.ref.path.address.host.get)))
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
    }
  }

}

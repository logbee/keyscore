package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.actor
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.scaladsl.{Flow, RunnableGraph}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import io.logbee.keyscore.agent.stream.FilterManager._
import io.logbee.keyscore.agent.stream.stage.DefaultFilterStage
import io.logbee.keyscore.agent.util.Reflection
import io.logbee.keyscore.commons.cluster.CreateNewStream
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.{FilterExtension, SinkExtension, SourceExtension}
import io.logbee.keyscore.commons.util.StartUpWatch.Ready
import io.logbee.keyscore.model.filter.{FilterFunction, MetaFilterDescriptor}
import io.logbee.keyscore.model.{Dataset, StreamConfiguration}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.control.Breaks._

class FilterManager extends Actor with ActorLogging {

  private val eventBus = context.system.eventStream
  private val filterLoader = new FilterLoader

  private val descriptors = mutable.HashMap.empty[String, FilterRegistration]

  private val filterKillSwitches = mutable.HashMap.empty[UUID, UniqueKillSwitch]

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[RegisterExtension])
    log.info("StartUp complete.")
  }

  override def postStop(): Unit = {
    eventBus.unsubscribe(self)
  }

  override def receive: Receive = {
    case CreateNewStream(streamID, streamSpec) =>
      breakable {
        log.info("Building stream with id: " + streamID)
        val streamBlueprint = try {
          createStreamFromModel(streamID, streamSpec)
        } catch {
          case nse: NoSuchElementException =>
            sender ! GraphBuildingAnswer(Some(GraphBuildException(streamID, streamSpec, errorMsg = nse.getMessage)))
            break
        }

        //TODO
      }

    case RegisterExtension(extensionType, extensionClass) =>
      log.info(s"Registering extension '$extensionClass' of type '$extensionType'.")
      extensionType match {
        case FilterExtension | SinkExtension | SourceExtension =>
          val descriptor = filterLoader.loadDescriptors(extensionClass)
          descriptors += (descriptor.name -> FilterRegistration(descriptor, extensionClass))
      }

    case GetDescriptors =>
      sender ! Descriptors(descriptors.values.map(_.filterDescriptor).toList)

    case Ready =>
      sender ! Ready
  }

  def createStreamFromModel(streamID: UUID, streamSpec: StreamConfiguration): StreamBlueprint = {
    try {
      //source

      //sink

      //filters
      val filterBuffer = scala.collection.mutable.Map[UUID, Flow[Dataset, Dataset, Future[DefaultFilterStage]]]()

      streamSpec.filter.foreach { filter =>
        var filterFunction = Reflection.createFilterByClassname(FilterRegistry.filters(filter.kind), filter).asInstanceOf[FilterFunction]
      }

      //Create Blueprint
      StreamBlueprint(streamID, filterBuffer.toMap)
    } catch {
      case nse: NoSuchElementException => throw nse
    }

  }

}

object FilterManager {
  def props()(implicit materializer: ActorMaterializer): Props = actor.Props(new FilterManager)

  case object GetDescriptors

  case class Descriptors(descriptors: List[MetaFilterDescriptor])

  trait GraphBuild {}

  case class GraphBuildingAnswer(answer: Option[GraphBuild])

  case class GraphBuilt(streamID: UUID, graph: RunnableGraph[UniqueKillSwitch]) extends GraphBuild

  case class GraphBuildException(streamID: UUID, streamSpec: StreamConfiguration, errorMsg: String) extends GraphBuild

  case class StreamBlueprint(streamID: UUID,
                             //TODO source
                             //TODO sink
                             filter: Map[UUID, Flow[Dataset, Dataset, Future[DefaultFilterStage]]]
                            )

}
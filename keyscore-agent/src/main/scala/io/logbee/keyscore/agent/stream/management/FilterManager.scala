package io.logbee.keyscore.agent.stream.management

import java.util.UUID

import akka.actor
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import io.logbee.keyscore.agent.stream.contrib.stages.DefaultFilterStage
import io.logbee.keyscore.agent.stream.management.FilterManager._
import io.logbee.keyscore.agent.stream.management.StreamSupervisor.CreateStream
import io.logbee.keyscore.agent.util.Reflection
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.{FilterExtension, SinkExtension, SourceExtension}
import io.logbee.keyscore.commons.util.StartUpWatch.Ready
import io.logbee.keyscore.model.filter.{FilterDescriptor, FilterFunction}
import io.logbee.keyscore.model.{Dataset, StreamModel}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.control.Breaks._

object FilterManager {
  def props()(implicit materializer: ActorMaterializer): Props = actor.Props(new FilterManager)

  case object GetDescriptors

  case class Descriptors(descriptors: List[FilterDescriptor])

  trait GraphBuild {}

  case class GraphBuildingAnswer(answer: Option[GraphBuild])

  case class GraphBuilt(streamID: UUID, graph: RunnableGraph[UniqueKillSwitch]) extends GraphBuild

  case class GraphBuildException(streamID: UUID, streamSpec: StreamModel, errorMsg: String) extends GraphBuild

  case class StreamBlueprint(streamID: UUID,
                             //TODO source
                             //TODO sink
                             filter: Map[UUID, Flow[Dataset, Dataset, Future[DefaultFilterStage]]]
                            )

}

case class FilterRegistration(filterDescriptor: FilterDescriptor, filterClass: Class[_])

class FilterManager(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

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
    case CreateStream(streamID, streamSpec) =>
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
          val descriptor = filterLoader.loadDescriptor(extensionClass)
          descriptors += (descriptor.name -> FilterRegistration(descriptor, extensionClass))
      }

    case GetDescriptors =>
      sender ! Descriptors(descriptors.values.map(_.filterDescriptor).toList)

    case Ready =>
      sender ! Ready
  }

  def createStreamFromModel(streamID: UUID, streamSpec: StreamModel): StreamBlueprint = {
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

//TODO Generate this dynamically
object FilterRegistry {

  val filters = Map(
    "KafkaSource" -> "io.logbee.keyscore.frontier.sources.KafkaSource",
    "HttpSource" -> "io.logbee.keyscore.frontier.sources.HttpSource",
    "KafkaSink" -> "io.logbee.keyscore.frontier.sinks.KafkaSink",
    "StdOutSink" -> "io.logbee.keyscore.frontier.sinks.StdOutSink",
    "CSVFilter" -> "io.logbee.keyscore.agent.stream.contrib.filter.CSVParserFilterFunction",
    "AddFieldsFilter" -> "io.logbee.keyscore.agent.stream.contrib.filter.AddFieldsFilterFunction",
    "RemoveFieldsFilter" -> "io.logbee.keyscore.agent.stream.contrib.filter.RemoveFieldsFilterFunction",
    "RetainFieldsFilter" -> "io.logbee.keyscore.agent.stream.contrib.filter.RetainFieldsFilterFunction",
    "GrokFilter" -> "io.logbee.keyscore.agent.stream.contrib.filter.GrokFilterFunction"
  )
}

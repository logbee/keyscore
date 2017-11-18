package io.logbee.keyscore.frontier.rest.api

import java.util.UUID

import akka.actor.Status.{Failure, Success}
import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import io.logbee.keyscore.frontier
import io.logbee.keyscore.frontier.rest.api.StreamManager._
import io.logbee.keyscore.frontier.{Filters, Streams}

import scala.collection.immutable.Seq
import scala.collection.mutable

object StreamManager {

  case class CreateStreamCommand(stream: frontier.Stream)
  case class DeleteStreamCommand(id: UUID)
  case class UpdateStreamCommand(id: UUID, stream: frontier.Stream)
  case object DeleteAllStreamsCommand
  case object GetAllStreams
  case class GetStream(id: UUID)
  case class GetAllFiltersFromStream(id: UUID)
  case class AddFilterToStreamCommand(streamId: UUID, filterId: UUID)
  case class RemoveFilterFromStreamCommand(streamId: UUID, filterId: UUID)
  case class RemoveAllFiltersFromStreamsCommand(streamId: UUID)

  case class StreamCreatedEvent(id: UUID, stream: frontier.Stream)
  case class StreamUpdatedEvent(id: UUID, stream: frontier.Stream)
  case class StreamDeletedEvent(id: UUID)
  case object AllStreamsDeletedEvent

  case class FilterAddedEvent(streamId: UUID, filterId: UUID)
  case class FilterRemovedEvent(streamId: UUID, filterId: UUID)

  def props: Props = Props[StreamManager]
}

class StreamManager extends PersistentActor with ActorLogging {

  override def persistenceId = "stream-manager"

  val streams: mutable.HashMap[UUID, frontier.Stream] = mutable.HashMap()
  val filters: mutable.HashMap[UUID, mutable.ListBuffer[UUID]] = mutable.HashMap()

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
      log.info("RecoveryCompleted")
    case event: StreamCreatedEvent =>
      streams.put(event.id, event.stream)
    case event: StreamUpdatedEvent =>
      streams.put(event.id, event.stream)
    case event: StreamDeletedEvent =>
      streams.remove(event.id)
    case AllStreamsDeletedEvent =>
      streams.clear()
  }

  override def receiveCommand: Receive = {
    case cmd: CreateStreamCommand =>
      val id = UUID.randomUUID()
      streams.put(id, cmd.stream)
      persist(StreamCreatedEvent(id, cmd.stream)) { _ =>
        sender() ! id
      }
    case cmd: UpdateStreamCommand =>
      streams.get(cmd.id) match {
        case Some(stream) =>
          streams.put(cmd.id, cmd.stream)
          persist(StreamUpdatedEvent(cmd.id, cmd.stream)) { _ =>
            sender() ! Success
          }
        case None => sender() ! Failure
      }
    case cmd: DeleteStreamCommand =>
        streams.remove(cmd.id) match {
        case None => sender() ! Failure
        case Some(_) =>
          persist(StreamDeletedEvent(cmd.id)) { _ =>
            sender() ! Success
          }
      }
    case DeleteAllStreamsCommand =>
      streams.clear()
      persist(AllStreamsDeletedEvent) { _ =>
        sender() ! Success
      }
    case msg: GetStream =>
        sender() ! streams.get(msg.id)
    case GetAllStreams =>
      sender() ! Streams(streams.keys.map(_.toString).to[Seq])
    case msg: GetAllFiltersFromStream =>
      sender() ! Filters(filters.get(msg.id).map(_.toString()).toSeq)
    case cmd: AddFilterToStreamCommand =>
      if (streams.contains(cmd.streamId)) {
        filters.get(cmd.streamId) match {
          case Some(list) =>
            list += cmd.filterId
          case None =>
            filters.put(cmd.streamId, mutable.ListBuffer(cmd.filterId))
        }
        persist(FilterAddedEvent(cmd.streamId, cmd.filterId)) { _ =>
          sender() ! Success
        }
      }
      else {
        sender() ! Failure
      }

    case cmd: RemoveFilterFromStreamCommand =>
      filters.get(cmd.streamId) match  {
        case Some(list) =>
          if (list.contains(cmd.filterId)) {
            list -= cmd.filterId
            persist(FilterRemovedEvent(cmd.streamId, cmd.filterId)) { _ =>
              sender() ! Success
            }
          }
          else {
            sender() ! Failure
          }
        case None => sender() ! Failure
      }
    case cmd: RemoveAllFiltersFromStreamsCommand =>
      filters.remove(cmd.streamId) match {
        case Some(list) => sender() ! Success
        case None => sender() ! Failure
      }
    case _ =>
      sender() ! Failure
  }
}


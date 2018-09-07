package io.logbee.keyscore.model.pipeline

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration

import scala.concurrent.Future

trait LogicProxy {
  val id: UUID
  def configure(configuration: Configuration): Future[FilterState]
  def state(): Future[FilterState]
}

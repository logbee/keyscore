package io.logbee.keyscore.agent.pipeline.controller

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.valve.ValveProxy
import io.logbee.keyscore.model.WhichValve
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.pipeline.{FilterState, MergeProxy}

import scala.concurrent.Future

/**
  * The '''MergeController''' manages all requests for a running ~Merge~. <br><br>
  * He can change the Configuration of his ~Merge~ on runtime, do live-edition operations or retrieve live-stats of the ~Merge~. 
  *
  * @todo Implement
  *
  * @param leftValve The left Valve before the ~Merge~
  * @param rightValve The right Valve before the ~Merge~
  * @param merge The corresponding ~Merge~
  * @param outValve The Valve before the ~Merge~
  */
private class MergeController(val leftValve: ValveProxy, val rightValve: ValveProxy, val merge: MergeProxy, val outValve: ValveProxy) extends Controller {

  override val id: UUID = merge.id

  override def configure(configuration: Configuration): Future[FilterState] = merge.configure(configuration)

  override def pause(doClose: Boolean): Future[FilterState] = ???

  override def drain(drain: Boolean): Future[FilterState] = ???

  override def insert(dataset: List[Dataset], whichValve: WhichValve): Future[FilterState] = ???

  override def extract(n: Int, whichValve: WhichValve): Future[List[Dataset]] = ???

  override def state(): Future[FilterState] = ???

  override def clear(): Future[FilterState] = ???
}

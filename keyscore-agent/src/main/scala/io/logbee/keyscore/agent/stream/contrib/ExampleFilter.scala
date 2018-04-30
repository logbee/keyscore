package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.filter._

import scala.concurrent.Future

object ExampleFilter extends Described {
  override val descriptor: FilterDescriptor = FilterDescriptor("ExampleFilter", "An Example Filter", List.empty)
}

class ExampleFilter extends Filter {

  override def changeCondition(trigger: FilterCondition): Future[Boolean] = ???

  override def changeFunction(function: FilterFunction): Future[Boolean] = ???

  override def configureCondition(configuration: FilterConfiguration): Future[Boolean] = ???

  override def configureFunction(configuration: FilterConfiguration): Future[Boolean] = ???
}

package io.logbee.keyscore.agent

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.management.FilterManager
import io.logbee.keyscore.agent.stream.management.FilterManager.{Descriptors, GetDescriptors}
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.FilterExtension
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.sink.FilterDescriptor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

class FilterManagerSpec extends TestKit(ActorSystem("spec")) with ImplicitSender with WordSpecLike with Matchers with ScalaFutures {

  "A FilterManager" should {

    val filterManager = system.actorOf(Props[FilterManager])

    implicit val timeout: Timeout = 5 seconds

    "load filter extensions " in {
      filterManager ! RegisterExtension(FilterExtension, classOf[ExampleFilter])

      filterManager ! GetDescriptors

      val message = receiveOne(5 seconds).asInstanceOf[Descriptors]
      message.descriptors should (contain (ExampleFilter.descriptor) and have length 1)
    }
  }
}

object ExampleFilter extends Described {
  override def descriptor: FilterDescriptor = FilterDescriptor("ExampleFilter", "An example filter", List.empty)
}

class ExampleFilter {

}

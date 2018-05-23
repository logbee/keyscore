package io.logbee.keyscore.agent

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.FilterManager
import io.logbee.keyscore.agent.stream.FilterManager.{DescriptorsResponse, RequestDescriptors}
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.FilterExtension
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.filter._
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class FilterManagerSpec extends TestKit(ActorSystem("spec")) with ImplicitSender with WordSpecLike with Matchers with ScalaFutures {

  "A FilterManager" should {

    val filterManager = system.actorOf(Props[FilterManager])

    implicit val timeout: Timeout = 5 seconds

    "load filter extensions " in {

      filterManager ! RegisterExtension(FilterExtension, classOf[ExampleFilter])
      filterManager ! RequestDescriptors

      val message = receiveOne(5 seconds).asInstanceOf[DescriptorsResponse]
      message.descriptors should (contain (ExampleFilter.describe) and have length 1)
    }
  }
}

object ExampleFilter extends Described {

  val filterName = "ExampleFilter"
  val filterId ="2b6e5fd0-a21b-4256-8a4a-388e3b4e5711"

  override def describe: MetaFilterDescriptor = {
    val descriptors = mutable.Map.empty[Locale, FilterDescriptorFragment]
    descriptors ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(fromString(filterId), filterName, descriptors.toMap)
  }

  def descriptor(language: Locale): FilterDescriptorFragment = {
    val filterText: ResourceBundle = ResourceBundle.getBundle(filterName, language)
    FilterDescriptorFragment(filterText.getString("displayName"), filterText.getString("description"),
      FilterConnection(true), FilterConnection(true), List(
        MapParameterDescriptor("fieldsToAdd", filterText.getString("fieldsToAddName"), filterText.getString("fieldsToAddDescription"),
          TextParameterDescriptor("fieldName", filterText.getString("fieldKeyName"), filterText.getString("fieldKeyDescription")),
          TextParameterDescriptor("fieldValue", filterText.getString("fieldValueName"), filterText.getString("fieldValueDescription"))
        )))
  }
}
class ExampleFilter {

}

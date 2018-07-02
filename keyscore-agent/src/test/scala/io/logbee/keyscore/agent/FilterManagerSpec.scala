package io.logbee.keyscore.agent

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle, UUID}

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import io.logbee.keyscore.agent.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.agent.extension.FilterExtension
import io.logbee.keyscore.agent.pipeline.FilterManager
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.stage.StageContext
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.filter._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class FilterManagerSpec extends TestKit(ActorSystem("spec")) with ImplicitSender with WordSpecLike with Matchers with ScalaFutures with MockFactory {
  "A FilterManager" should {

    val filterManager = system.actorOf(Props[FilterManager])

    implicit val timeout: Timeout = 5 seconds

    "load filter extensions " in {

      filterManager ! RegisterExtension(FilterExtension, Option(classOf[ExampleFilter]))
      filterManager ! RequestDescriptors

      val response = receiveOne(5 seconds).asInstanceOf[DescriptorsResponse]
      response.descriptors should (contain (ExampleFilter.describe) and have length 1)
    }

    "instantiate a filter stage" in {
      val filterConfiguration = FilterConfiguration(UUID.randomUUID(),FilterDescriptor(fromString("2b6e5fd0-a21b-4256-8a4a-388e3b4e5711"),"ExampleFilter",List(
        MapParameterDescriptor("fieldsToAdd", "fieldsToAddName", "fieldsToAddDescription",
          TextParameterDescriptor("fieldName", "fieldKeyName","fieldKeyDescription"),
          TextParameterDescriptor("fieldValue", "fieldValueName", "fieldValueDescription")
        ))),List(TextMapParameter("fieldsToAdd",Map("blubb"->"lappen","lappen"->"blubb"))))

      val result = Await.ready(filterManager ? CreateFilterStage(StageContext(system,system.dispatcher),filterConfiguration),10 seconds)


      result shouldBe a [Future[_]]

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

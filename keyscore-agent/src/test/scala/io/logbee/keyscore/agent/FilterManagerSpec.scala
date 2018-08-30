package io.logbee.keyscore.agent

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.stage.StageContext
import io.logbee.keyscore.agent.pipeline.{ExampleFilter, FilterManager}
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.FilterExtension
import io.logbee.keyscore.model.configuration.Configuration
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpecLike, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class FilterManagerSpec extends TestKit(ActorSystem("spec")) with ImplicitSender with FreeSpecLike with Matchers with ScalaFutures with MockFactory {

  implicit val timeout: Timeout = 5 seconds

  "A FilterManager" - {

    val filterManager = system.actorOf(Props[FilterManager])

    "should load filter extensions " in {

      filterManager ! RegisterExtension(FilterExtension, classOf[ExampleFilter])
      filterManager ! RequestDescriptors

      val message = receiveOne(5 seconds).asInstanceOf[DescriptorsResponse]
      message.descriptors should (contain(ExampleFilter.describe) and have length 1)
    }

    "should instantiate a filter stage" in {

      val result = Await.ready(filterManager ? CreateFilterStage(StageContext(system, system.dispatcher), ExampleFilter.describe, Configuration()), 10 seconds)

      result shouldBe a[Future[_]]
    }
  }

  "A FilterManager with registered extensions" - {

    val filterManager = system.actorOf(Props[FilterManager])
    val ctx = StageContext(system, system.dispatcher)

    filterManager ! RegisterExtension(FilterExtension, classOf[ExampleFilter])

    "should create a sink stage" in {

      filterManager ! CreateSinkStage(ctx, ExampleFilter.describe, Configuration.empty)

      val message = receiveOne(5 seconds).asInstanceOf[SinkStageCreated]

      message.stage should not be (null)
    }

    "should create a source stage" in {

      filterManager ! CreateSourceStage(ctx, ExampleFilter.describe, Configuration.empty)

      val message = receiveOne(5 seconds).asInstanceOf[SourceStageCreated]

      message.stage should not be (null)
    }

    "should create a filter stage" in {

      filterManager ! CreateFilterStage(ctx, ExampleFilter.describe, Configuration.empty)

      val message = receiveOne(5 seconds).asInstanceOf[FilterStageCreated]

      message.stage should not be (null)
    }

  }
}

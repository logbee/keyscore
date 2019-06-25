package io.logbee.keyscore.agent.pipeline

import java.util.UUID.randomUUID

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.FilterExtension
import io.logbee.keyscore.model.blueprint.BlueprintRef
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidToString
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.stage.StageContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpecLike, Matchers}

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

      val result = Await.ready(filterManager ? CreateFilterStage(BlueprintRef(randomUUID()), StageSupervisor.noop, StageContext(system, system.dispatcher), ExampleFilter.describe.ref, Configuration()), 10 seconds)

      result shouldBe a[Future[_]]
    }
  }

  "A FilterManager with registered extensions" - {

    val filterManager = system.actorOf(Props[FilterManager])
    val ctx = StageContext(system, system.dispatcher)

    filterManager ! RegisterExtension(FilterExtension, classOf[ExampleFilter])

    "should create a sink stage" in {

      filterManager ! CreateSinkStage(BlueprintRef(randomUUID()), StageSupervisor.noop, ctx , ExampleFilter.describe.ref, Configuration.empty)

      val message = receiveOne(5 seconds).asInstanceOf[SinkStageCreated]

      message.stage should not be (null)
    }

    "should create a source stage" in {

      filterManager ! CreateSourceStage(BlueprintRef(randomUUID()), StageSupervisor.noop, ctx, ExampleFilter.describe.ref, Configuration.empty)

      val message = receiveOne(5 seconds).asInstanceOf[SourceStageCreated]

      message.stage should not be (null)
    }

    "should create a filter stage" in {

      filterManager ! CreateFilterStage(BlueprintRef(randomUUID()), StageSupervisor.noop, ctx, ExampleFilter.describe.ref, Configuration.empty)

      val message = receiveOne(5 seconds).asInstanceOf[FilterStageCreated]

      message.stage should not be (null)
    }

    "should create a branch stage" in {

      filterManager ! CreateBranchStage(BlueprintRef(randomUUID()), StageSupervisor.noop, ctx, ExampleFilter.describe.ref, Configuration.empty)

      val message = receiveOne(5 seconds).asInstanceOf[BranchStageCreated]

      message.stage should not be (null)
    }

    "should create a merge stage" in {

      filterManager ! CreateMergeStage(BlueprintRef(randomUUID()), StageSupervisor.noop, ctx, ExampleFilter.describe.ref, Configuration.empty)

      val message = receiveOne(5 seconds).asInstanceOf[MergeStageCreated]

      message.stage should not be (null)
    }
  }
}

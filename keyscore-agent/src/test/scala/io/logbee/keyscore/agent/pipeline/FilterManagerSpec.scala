package io.logbee.keyscore.agent.pipeline

import java.util.UUID.randomUUID

import akka.actor.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.testkit.TestProbe
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.FilterManager.{DescriptorNotFound, StageCreationFailed}
import io.logbee.keyscore.agent.pipeline.examples._
import io.logbee.keyscore.agent.runtimes.api.StageLogicProvider
import io.logbee.keyscore.agent.runtimes.api.StageLogicProvider.{Load, LoadSuccess}
import io.logbee.keyscore.model.blueprint.BlueprintRef
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidToString
import io.logbee.keyscore.model.descriptor.DescriptorRef
import io.logbee.keyscore.model.pipeline.StageSupervisor._
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage._
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Inside, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration._
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class FilterManagerSpec extends FreeSpec with Matchers with Inside {

  implicit val timeout: Timeout = 5 seconds

  import io.logbee.keyscore.test.fixtures.ToActorRef._
  import io.logbee.keyscore.test.fixtures._

  val descriptors = List(ExampleSource.describe, ExampleSink.describe, ExampleFilter.describe, ExampleBranch.describe, ExampleMerge.describe)

  "A FilterManager" - {

    "should load Extensions from providers" in withFixture { fixture =>

      import fixture._

      provider.expectMsg(Load(testee))

      testee ! LoadSuccess(descriptors, provider)
    }

    "when initialized" - {

      "should tell about unknown descriptors" in withFixture { implicit fixture =>
        import fixture._

        withInitializedFilterManager { _ =>

          val descriptorRef = DescriptorRef(randomUUID())

          testee tell(FilterManager.CreateSinkStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef))

          testee tell(FilterManager.CreateSourceStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef))

          testee tell(FilterManager.CreateFilterStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef))

          testee tell(FilterManager.CreateMergeStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef))

          testee tell(FilterManager.CreateBranchStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef))
        }
      }

      "should tell about stage creation failures" in withFixture { implicit fixture =>
        import fixture._

        provider.expectMsg(Load(testee))

        testee ! LoadSuccess(List(MalformedExampleFilter.describe), provider)

        val descriptorRef = MalformedExampleFilter.describe.ref

        testee tell(FilterManager.CreateSinkStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
        inside(provider.expectMsgType[StageLogicProvider.CreateSinkStage]) {
          case StageLogicProvider.CreateSinkStage(`descriptorRef`, `parameters`, replyTo) =>
            replyTo ! StageLogicProvider.StageCreationFailed(descriptorRef, blueprintRef, provider)
        }
        sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef))

        testee tell(FilterManager.CreateSourceStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
        inside(provider.expectMsgType[StageLogicProvider.CreateSourceStage]) {
          case StageLogicProvider.CreateSourceStage(`descriptorRef`, `parameters`, replyTo) =>
            replyTo ! StageLogicProvider.StageCreationFailed(descriptorRef, blueprintRef, provider)
        }
        sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef))

        testee tell(FilterManager.CreateFilterStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
        inside(provider.expectMsgType[StageLogicProvider.CreateFilterStage]) {
          case StageLogicProvider.CreateFilterStage(`descriptorRef`, `parameters`, replyTo) =>
            replyTo ! StageLogicProvider.StageCreationFailed(descriptorRef, blueprintRef, provider)
        }
        sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef))

        testee tell(FilterManager.CreateBranchStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
        inside(provider.expectMsgType[StageLogicProvider.CreateBranchStage]) {
          case StageLogicProvider.CreateBranchStage(`descriptorRef`, `parameters`, replyTo) =>
            replyTo ! StageLogicProvider.StageCreationFailed(descriptorRef, blueprintRef, provider)
        }
        sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef))

        testee tell(FilterManager.CreateMergeStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)
        inside(provider.expectMsgType[StageLogicProvider.CreateMergeStage]) {
          case StageLogicProvider.CreateMergeStage(`descriptorRef`, `parameters`, replyTo) =>
            replyTo ! StageLogicProvider.StageCreationFailed(descriptorRef, blueprintRef, provider)
        }
        sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef))
      }

      "should create a sink stage" in withFixture { implicit fixture =>
        import fixture._

        withInitializedFilterManager { _ =>

          val descriptorRef = ExampleSink.describe.ref
          val stage = new SinkStage(parameters, null)

          testee tell(FilterManager.CreateSinkStage(blueprintRef, noop, ctx, descriptorRef, parameters.configuration), sender)

          inside(provider.expectMsgType[StageLogicProvider.CreateSinkStage]) {
            case StageLogicProvider.CreateSinkStage(`descriptorRef`, `parameters`, replyTo) =>
              replyTo ! StageLogicProvider.SinkStageCreated(descriptorRef, stage, provider)
          }

          sender.expectMsg(FilterManager.SinkStageCreated(blueprintRef, stage))
        }
      }

      "should create a source stage" in withFixture { implicit fixture =>
        import fixture._

        withInitializedFilterManager { _ =>

          val descriptorRef = ExampleSource.describe.ref
          val stage = new SourceStage(parameters, null)

          testee tell(FilterManager.CreateSourceStage(blueprintRef, noop, ctx, ExampleSource.describe.ref, parameters.configuration), sender)

          inside(provider.expectMsgType[StageLogicProvider.CreateSourceStage]) {
            case StageLogicProvider.CreateSourceStage(`descriptorRef`, `parameters`, replyTo) =>
              replyTo ! StageLogicProvider.SourceStageCreated(descriptorRef, stage, provider)
          }

          sender.expectMsg(FilterManager.SourceStageCreated(blueprintRef, stage))
        }
      }

      "should create a filter stage" in withFixture { implicit fixture =>
        import fixture._

        withInitializedFilterManager { _ =>

          val descriptorRef = ExampleFilter.describe.ref
          val stage = new FilterStage(parameters, null)

          testee tell(FilterManager.CreateFilterStage(blueprintRef, noop, ctx, ExampleFilter.describe.ref, parameters.configuration), sender)

          inside(provider.expectMsgType[StageLogicProvider.CreateFilterStage]) {
            case StageLogicProvider.CreateFilterStage(`descriptorRef`, `parameters`, replyTo) =>
              replyTo ! StageLogicProvider.FilterStageCreated(descriptorRef, stage, provider)
          }

          sender.expectMsg(FilterManager.FilterStageCreated(blueprintRef, stage))

        }
      }

      "should create a branch stage" in withFixture { implicit fixture =>
        import fixture._

        withInitializedFilterManager { _ =>

          val descriptorRef = ExampleBranch.describe.ref
          val stage = new BranchStage(parameters, null)

          testee tell(FilterManager.CreateBranchStage(blueprintRef, noop, ctx, ExampleBranch.describe.ref, parameters.configuration), sender)

          inside(provider.expectMsgType[StageLogicProvider.CreateBranchStage]) {
            case StageLogicProvider.CreateBranchStage(`descriptorRef`, `parameters`, replyTo) =>
              replyTo ! StageLogicProvider.BranchStageCreated(descriptorRef, stage, provider)
          }

          sender.expectMsg(FilterManager.BranchStageCreated(blueprintRef, stage))
        }
      }

      "should create a merge stage" in withFixture { implicit fixture =>
        import fixture._

        withInitializedFilterManager { _ =>

          val descriptorRef = ExampleMerge.describe.ref
          val stage = new MergeStage(parameters, null)

          testee tell(FilterManager.CreateMergeStage(blueprintRef, noop, ctx, ExampleMerge.describe.ref, parameters.configuration), sender)

          inside(provider.expectMsgType[StageLogicProvider.CreateMergeStage]) {
            case StageLogicProvider.CreateMergeStage(`descriptorRef`, `parameters`, replyTo) =>
              replyTo ! StageLogicProvider.MergeStageCreated(descriptorRef, stage, provider)
          }

          sender.expectMsg(FilterManager.MergeStageCreated(blueprintRef, stage))
        }
      }
    }
  }

  def withFixture(test: Fixture => Any): Any = {

    withActorSystem { implicit system =>

      test(new Fixture {
        override val provider: TestProbe = TestProbe("provider")
        override val sender: TestProbe =  TestProbe("sender")
        override val testee: ActorRef = system.actorOf(FilterManager(List(provider)), "filter-manager")
        override val ctx: StageContext = StageContext(system, system.dispatcher)
        override val parameters: LogicParameters = LogicParameters(randomUUID(), noop, ctx, Configuration())
        override val blueprintRef: BlueprintRef = BlueprintRef(parameters.uuid)

      })
    }
  }

  trait Fixture {
    val provider: TestProbe
    val sender: TestProbe
    val testee: ActorRef
    val ctx: StageContext
    val parameters: LogicParameters
    val blueprintRef: BlueprintRef
  }

  def withInitializedFilterManager(test: Any => Any)(implicit fixture: Fixture): Any = {

    import fixture._

    provider.expectMsg(Load(testee))

    testee ! LoadSuccess(descriptors, provider)

    test(())
  }
}

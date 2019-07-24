package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID
import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.testkit.TestProbe
import io.logbee.keyscore.agent.pipeline.examples._
import io.logbee.keyscore.agent.pipeline.stage.StageLogicProvider._
import io.logbee.keyscore.model.blueprint.BlueprintRef
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidToString
import io.logbee.keyscore.model.descriptor.DescriptorRef
import io.logbee.keyscore.model.pipeline.StageSupervisor.noop
import io.logbee.keyscore.model.util.Using.using
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage._
import io.logbee.keyscore.test.fixtures.ToActorRef.Probe2TypedActorRef
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Inside, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ManifestStageLogicProviderSpec extends FreeSpec with Matchers with Inside {

  import akka.actor.typed.scaladsl.adapter._
  import io.logbee.keyscore.test.fixtures._

  "A ManifestStageLogicProvider" - {

    "should reply when loaded" in withActorSystem { implicit system =>

      withFixture { (testee, sender) =>

        testee ! Load(sender)

        sender.expectMsg(LoadSuccess(List(ExampleSource.describe,
                                     ExampleMerge.describe,
                                     ExampleBranch.describe,
                                     ExampleFilter.describe,
                                     ExampleSink.describe), testee))
      }
    }

    "should reply with empty descriptor List" in withActorSystem { implicit system =>

      withFixtureEmptyManifest { (testee, sender) =>

        testee ! Load(sender)

        sender.expectMsg(LoadSuccess(List(), testee))
      }
    }

    "when initialized" - {

      "should tell about malformed logics" in withActorSystem { implicit system =>

        whenInitializedWithMalformed { (testee, sender) =>

          val descriptorRef = MalformedExampleFilter.describe.ref
          val blueprintRef = BlueprintRef(UUID.randomUUID())

          testee ! CreateSourceStage(descriptorRef, LogicParameters(blueprintRef, null, null, null), sender)
          sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef, testee))

          testee ! CreateSinkStage(descriptorRef, LogicParameters(blueprintRef, null, null, null), sender)
          sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef, testee))

          testee ! CreateFilterStage(descriptorRef, LogicParameters(blueprintRef, null, null, null), sender)
          sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef, testee))

          testee ! CreateBranchStage(descriptorRef, LogicParameters(blueprintRef, null, null, null), sender)
          sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef, testee))

          testee ! CreateMergeStage(descriptorRef, LogicParameters(blueprintRef, null, null, null), sender)
          sender.expectMsg(StageCreationFailed(descriptorRef, blueprintRef, testee))
        }
      }

      "should reply DescriptorNotFound" in withActorSystem { implicit system =>

        whenInitialized { (testee, sender) =>

          val descriptorRef = DescriptorRef(UUID.randomUUID())
          val blueprintRef = BlueprintRef(UUID.randomUUID())
          val context = StageContext(system, system.dispatcher)
          val parameters = LogicParameters(blueprintRef, noop, context, Configuration())

          testee ! CreateSourceStage(descriptorRef, parameters, sender)

          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef, testee))

          testee ! CreateSinkStage(descriptorRef, parameters, sender)

          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef, testee))

          testee ! CreateFilterStage(descriptorRef, parameters, sender)

          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef, testee))

          testee ! CreateMergeStage(descriptorRef, parameters, sender)

          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef, testee))

          testee ! CreateBranchStage(descriptorRef, parameters, sender)

          sender.expectMsg(DescriptorNotFound(descriptorRef, blueprintRef, testee))
        }
      }

      "should load the requested SourceStage" in withActorSystem { implicit  system =>

        whenInitialized { (testee, sender) =>
          val descriptorRef = ExampleSource.describe.ref
          val context = StageContext(system, system.dispatcher)
          val parameters = LogicParameters(randomUUID(), noop, context, Configuration())

          testee ! CreateSourceStage(descriptorRef, parameters, sender)

          inside(sender.expectMsgType[SourceStageCreated]) {
            case SourceStageCreated(`descriptorRef`, stage, `testee`) =>
              stage should not be null
              stage shouldBe a [SourceStage]
          }
        }
      }

      "should load the requested SinkStage" in withActorSystem { implicit  system =>

        whenInitialized { (testee, sender) =>
          val descriptorRef = ExampleSink.describe.ref
          val context = StageContext(system, system.dispatcher)
          val parameters = LogicParameters(randomUUID(), noop, context, Configuration())

          testee ! CreateSinkStage(descriptorRef, parameters, sender)

          inside(sender.expectMsgType[SinkStageCreated]) {
            case SinkStageCreated(`descriptorRef`, stage, `testee`) =>
              stage should not be null
              stage shouldBe a [SinkStage]
          }
        }
      }

      "should load the requested FilterStage" in withActorSystem { implicit system =>

        whenInitialized { (testee, sender) =>

          val descriptorRef = ExampleFilter.describe.ref
          val context = StageContext(system, system.dispatcher)
          val parameters = LogicParameters(randomUUID(), noop, context, Configuration())

          testee ! CreateFilterStage(descriptorRef, parameters, sender)

          inside(sender.expectMsgType[FilterStageCreated]) {
            case FilterStageCreated(`descriptorRef`, stage, `testee`) =>
              stage should not be null
              stage shouldBe a [FilterStage]
          }
        }
      }

      "should load the requested MergeStage" in withActorSystem { implicit  system =>

        whenInitialized { (testee, sender) =>
          val descriptorRef = ExampleMerge.describe.ref
          val context = StageContext(system, system.dispatcher)
          val parameters = LogicParameters(randomUUID(), noop, context, Configuration())

          testee ! CreateMergeStage(descriptorRef, parameters, sender)

          inside(sender.expectMsgType[MergeStageCreated]) {
            case MergeStageCreated(`descriptorRef`, stage, `testee`) =>
              stage should not be null
              stage shouldBe a [MergeStage]
          }
        }
      }

      "should load the requested BranchStage" in withActorSystem { implicit  system =>

        whenInitialized { (testee, sender) =>
          val descriptorRef = ExampleBranch.describe.ref
          val context = StageContext(system, system.dispatcher)
          val parameters = LogicParameters(randomUUID(), noop, context, Configuration())

          testee ! CreateBranchStage(descriptorRef, parameters, sender)

          inside(sender.expectMsgType[BranchStageCreated]) {
            case BranchStageCreated(`descriptorRef`, stage, `testee`) =>
              stage should not be null
              stage shouldBe a [BranchStage]
          }
        }
      }
    }
  }

  def whenInitialized(test: (ActorRef[StageLogicProviderRequest], TestProbe) => Any)(implicit system: ActorSystem): Any = {

    withFixture { (testee, sender) =>

      testee ! Load(sender)
      sender.expectMsgType[LoadSuccess]

      test(testee, sender)
    }
  }

  def whenInitializedWithMalformed(test: (ActorRef[StageLogicProviderRequest], TestProbe) => Any)(implicit system: ActorSystem): Any = {

    val sender = TestProbe("sender")
    val manifests = List(using(getClass.getResource("/malformed.filter.mf").openStream())(stream => new java.util.jar.Manifest(stream)))
    val testee = system.actorOf(ManifestStageLogicProvider(() => manifests))

    testee ! Load(sender)
    sender.expectMsgType[LoadSuccess]

    test(testee.toTyped, sender)
  }

  def withFixture(test: (ActorRef[StageLogicProviderRequest], TestProbe) => Any)(implicit system: ActorSystem): Any = {

    val sender = TestProbe("sender")
    val manifests = List(using(getClass.getResource("/example.manifest.mf").openStream())(stream => new java.util.jar.Manifest(stream)))
    val testee = system.actorOf(ManifestStageLogicProvider(() => manifests))

    test(testee.toTyped, sender)
  }

  def withFixtureEmptyManifest(test: (ActorRef[StageLogicProviderRequest], TestProbe) => Any)(implicit system: ActorSystem): Any = {

    val sender = TestProbe("sender")
    val manifests = List(using(getClass.getResource("/example.empty.manifest.mf").openStream())(stream => new java.util.jar.Manifest(stream)))
    val testee = system.actorOf(ManifestStageLogicProvider(() => manifests))

    test(testee.toTyped, sender)
  }

  def withFixtureUnknownClassManifest(test: (ActorRef[StageLogicProviderRequest], TestProbe) => Any)(implicit system: ActorSystem): Any = {

    val sender = TestProbe("sender")
    val manifests = List(using(getClass.getResource("/example.unknown_class.manifest.mf").openStream())(stream => new java.util.jar.Manifest(stream)))
    val testee = system.actorOf(ManifestStageLogicProvider(() => manifests))

    test(testee.toTyped, sender)
  }
}

package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.testkit.TestProbe
import io.logbee.keyscore.agent.pipeline.ExampleFilter
import io.logbee.keyscore.agent.pipeline.stage.StageLogicProvider._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.pipeline.StageSupervisor.noop
import io.logbee.keyscore.model.util.Using.using
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.test.fixtures.ToActorRef.Probe2TypedActorRef
import org.junit.runner.RunWith
import org.scalatest.{FreeSpec, Inside, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ManifestStageLogicProviderSpec extends FreeSpec with Matchers with Inside {

  import akka.actor.typed.scaladsl.adapter._

  "A ManifestStageLogicProvider" - {

    "should reply when loaded" in withActorSystem { implicit system =>

      withFixture { (testee, sender) =>

        testee ! Load(sender)

        sender.expectMsg(Loaded(List(ExampleFilter.describe), testee))
      }
    }

    "when initialized" - {

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
    }
  }

  def whenInitialized(test: (ActorRef[StageLogicProviderRequest], TestProbe) => Any)(implicit system: ActorSystem): Any = {

    withFixture { (testee, sender) =>

      testee ! Load(sender)
      sender.expectMsgType[Loaded]

      test(testee, sender)
    }
  }

  def withFixture(test: (ActorRef[StageLogicProviderRequest], TestProbe) => Any)(implicit system: ActorSystem): Any = {

    val sender = TestProbe("sender")
    val manifests = List(using(getClass.getResource("/example.manifest.mf").openStream())(stream => new java.util.jar.Manifest(stream)))
    val testee = system.actorOf(ManifestStageLogicProvider(() => manifests))

    test(testee.toTyped, sender)
  }

  def withActorSystem(test: ActorSystem => Any): Any = {

    val system = ActorSystem()

    try {
      test(system)
    }
    finally {
      Await.ready(system.terminate(), 10 seconds)
    }
  }
}

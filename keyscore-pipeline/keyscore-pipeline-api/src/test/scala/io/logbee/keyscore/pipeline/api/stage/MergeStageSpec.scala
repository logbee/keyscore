package io.logbee.keyscore.pipeline.api.stage

import java.util.UUID

import akka.stream.ClosedShape
import akka.stream.scaladsl.{GraphDSL, RunnableGraph}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.{LogicParameters, MergeLogic, MergeShape}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Promise
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class MergeStageSpec extends AnyFreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A MergeStage" - {

    val context = StageContext(system, executionContext)
    val configurationA = Configuration()
    val configurationB = Configuration()

    trait TestGraph {

      val provider: (LogicParameters, MergeShape[Dataset, Dataset, Dataset]) => MergeLogic

      lazy val stage = new MergeStage(LogicParameters(UUID.randomUUID(), StageSupervisor.noop, context, configurationA), provider)

      lazy val (left, right, mergeFuture, sink) = RunnableGraph.fromGraph(
        GraphDSL.create(TestSource.probe[Dataset], TestSource.probe[Dataset], stage, TestSink.probe[Dataset]) { (left, right, merge, sink) =>
          (left, right, merge, sink)
        } { implicit builder => (left, right, merge, sink) =>
          import GraphDSL.Implicits._

          left.out ~> merge.left
          right.out ~> merge.right
          merge.out ~> sink.in

          ClosedShape
        }).run()
    }

    "should pass the appropriate configuration to its logic" in new TestGraph {

      val updateConfiguration = Promise[Configuration]
      val initializeConfiguration = Promise[Configuration]

      val provider = (parameters: LogicParameters, shape: MergeShape[Dataset, Dataset, Dataset]) => new MergeLogic(parameters, shape) {

        override def initialize(configuration: Configuration): Unit = {
          initializeConfiguration.success(configuration)
        }

        override def configure(configuration: Configuration): Unit = {
          updateConfiguration.success(configuration)
        }

        override def onPush(): Unit = {}

        override def onPull(): Unit = {}
      }

      whenReady(mergeFuture) { branch =>
        branch.configure(configurationB)
      }

      whenReady(initializeConfiguration.future) { success =>
        success shouldBe configurationA
      }

      whenReady(updateConfiguration.future) { success =>
        success shouldBe configurationB
      }
    }

    "should pass the merged dataset to the output" in new TestGraph {

      val provider = (parameters: LogicParameters, shape: MergeShape[Dataset, Dataset, Dataset]) => new MergeLogic(parameters, shape) {

        override def initialize(configuration: Configuration): Unit = {}

        override def configure(configuration: Configuration): Unit = {}

        override def onPush(): Unit = {
          if (isAvailable(left) && isAvailable(right)) {
            push(out, Dataset(grab(left).records ++ grab(right).records))
          }
        }

        override def onPull(): Unit = {
          if (!hasBeenPulled(left)) pull(left)
          if (!hasBeenPulled(right)) pull(right)
        }
      }

      val sampleA = Dataset(Record(Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C"))))
      val sampleB = Dataset(Record(Field("temperture", DecimalValue(-11.5))))

      whenReady(mergeFuture) { merge =>

        left.sendNext(sampleA)
        right.sendNext(sampleB)

        sink.request(1)

        sink.expectNext(Dataset(sampleA.records ++ sampleB.records))
      }
    }
  }
}

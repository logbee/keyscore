package io.logbee.keyscore.pipeline.api.stage

import java.util.UUID

import akka.stream.ClosedShape
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.GraphDSL
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.{BranchLogic, BranchShape, LogicParameters}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Promise
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class BranchStageSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A BranchStage" - {

    val context = StageContext(system, executionContext)
    val configurationA = Configuration()
    val configurationB = Configuration()

    trait TestGraph {

      val provider: (LogicParameters, BranchShape[Dataset, Dataset, Dataset]) => BranchLogic

      lazy val stage = new BranchStage(LogicParameters(UUID.randomUUID(), StageSupervisor.noop, context, configurationA), provider)

      lazy val (source, branchFuture, left, right) = RunnableGraph.fromGraph(
        GraphDSL.create(TestSource.probe[Dataset], stage, TestSink.probe[Dataset], TestSink.probe[Dataset]) { (source, branch, left, right) =>
          (source, branch, left, right)
        } { implicit builder => (source, branch, left, right) =>
          import GraphDSL.Implicits._

          source.out ~> branch.in
          branch.left ~> left.in
          branch.right ~> right.in

          ClosedShape
        }).run(materializer)
    }

    "should pass the appropriate configuration to its logic" in new TestGraph {

      val updateConfiguration = Promise[Configuration]
      val initializeConfiguration = Promise[Configuration]

      val provider = (parameters: LogicParameters, shape: BranchShape[Dataset, Dataset, Dataset]) => new BranchLogic(parameters, shape) {

        override def initialize(configuration: Configuration): Unit = {
          initializeConfiguration.success(configuration)
        }

        override def configure(configuration: Configuration): Unit = {
          updateConfiguration.success(configuration)
        }

        override def onPush(): Unit = {}

        override def onPull(): Unit = {}
      }

      whenReady(branchFuture) { branch =>
        branch.configure(configurationB)
      }

      whenReady(initializeConfiguration.future) { success =>
        success shouldBe configurationA
      }

      whenReady(updateConfiguration.future) { success =>
        success shouldBe configurationB
      }
    }

    "should pass dataset to the left and right outputs" in new TestGraph {

      val provider = (parameters: LogicParameters, shape: BranchShape[Dataset, Dataset, Dataset]) => new BranchLogic(parameters, shape) {

        override def initialize(configuration: Configuration): Unit = {}

        override def configure(configuration: Configuration): Unit = {}

        override def onPush(): Unit = {
          val dataset = grab(in)
          push(left, dataset)
          push(right, dataset)
        }

        override def onPull(): Unit = {
          if (isAvailable(left) && isAvailable(right)) {
            pull(in)
          }
        }
      }

      val sample = Dataset(Record(Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C"))))

      whenReady(branchFuture) { branch =>

        left.request(1)
        right.request(1)

        source.sendNext(sample)

        left.expectNext(sample)
        right.expectNext(sample)
      }
    }
  }
}

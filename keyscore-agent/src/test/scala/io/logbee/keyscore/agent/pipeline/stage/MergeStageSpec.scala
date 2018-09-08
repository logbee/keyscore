package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.stream.ClosedShape
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.GraphDSL
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.commons.test.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.Promise
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class MergeStageSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A MergeStage" - {

    val context = StageContext(system, executionContext)

    val configurationA = Configuration()
    val configurationB = Configuration()

    val updateConfiguration = Promise[Configuration]
    val initializeConfiguration = Promise[Configuration]

    val provider = (parameters: LogicParameters, shape: MergeShape[Dataset, Dataset, Dataset]) => new MergeLogic(parameters, shape) {

      override def initialize(configuration: Configuration): Unit = {
        initializeConfiguration.success(configuration)
      }

      override def configure(configuration: Configuration): Unit = {
        updateConfiguration.success(configuration)
      }

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

    trait TestGraph {

      val stage = new MergeStage(LogicParameters(UUID.randomUUID(), context, configurationA), provider)

      val (left, right, mergeFuture, sink) = RunnableGraph.fromGraph(
        GraphDSL.create(TestSource.probe[Dataset], TestSource.probe[Dataset], stage, TestSink.probe[Dataset]) { (left, right, merge, sink) =>
          (left, right, merge, sink)
        } { implicit builder => (left, right, merge, sink) =>
          import GraphDSL.Implicits._

          left.out ~> merge.left
          right.out ~> merge.right
          merge.out ~> sink.in

          ClosedShape
        }).run(materializer)
    }

    "should pass the appropriate configuration to it's logic" in new TestGraph {

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

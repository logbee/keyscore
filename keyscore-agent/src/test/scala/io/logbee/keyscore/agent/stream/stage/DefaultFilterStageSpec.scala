package io.logbee.keyscore.agent.stream.stage

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.agent.stream.ExampleData._
import io.logbee.keyscore.agent.stream.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/*
@RunWith(classOf[JUnitRunner])
class DefaultFilterStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {
    val (filterFuture, probe) = Source(List(dataset1, dataset2))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A filter stage" should {

    "pass elements unmodified, if no filter and condition is specified" in new TestStream {

      whenReady(filterFuture) { filter =>
        probe.request(2)
        probe.expectNext(dataset1)
        probe.expectNext(dataset2)
      }
    }

    "pass elements unmodified, if the configured condition does not accept the elements" in new TestStream {

      whenReady(filterFuture) { filter =>

        val condition = stub[Condition]
        condition.apply _ when dataset1 returns Reject(dataset1)
        condition.apply _ when dataset2 returns Reject(dataset2)

        whenReady(filter.changeCondition(condition)) { result =>
          probe.request(2)
          probe.expectNext(dataset1)
          probe.expectNext(dataset2)
        }
      }
    }

    "pass a element modified, if the configured condition accepts it" in new TestStream {

      whenReady(filterFuture) { filter =>

        val condition = stub[Condition]
        val function = stub[FilterFunction]

        condition.apply _ when dataset1 returns Accept(dataset1)
        condition.apply _ when dataset2 returns Reject(dataset2)

        function.apply _ when dataset1 returns dataset1Modified
        function.apply _ when dataset2 returns dataset2Modified

        Await.ready(filter.changeCondition(condition), 10 seconds)
        Await.ready(filter.changeFunction(function), 10 seconds)

        probe.request(2)
        probe.expectNext(dataset1Modified)
        probe.expectNext(dataset2)
      }
    }

    "pass a changed condition configuration to the condition-instance" in new TestStream {

      whenReady(filterFuture) { filter =>

        val condition = stub[Condition]

        Await.ready(filter.changeCondition(condition), 30 seconds)
        Await.ready(filter.configureCondition(configA), 30 seconds)
        Await.ready(filter.configureCondition(configB), 30 seconds)

        condition.configure _ verify configA
        condition.configure _ verify configB
      }
    }

    "pass a changed function configuration to the function-instance" in new TestStream {

      whenReady(filterFuture) { filter =>

        val function = stub[FilterFunction]

        Await.ready(filter.changeFunction(function), 30 seconds)
        Await.ready(filter.configureFunction(configA), 30 seconds)
        Await.ready(filter.configureFunction(configB), 30 seconds)

        function.configure _ verify configA
        function.configure _ verify configB
      }
    }
  }
}
*/
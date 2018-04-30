package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import io.logbee.keyscore.agent.stream.ExampleData._
import io.logbee.keyscore.model.sink.SinkFunction
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.language.postfixOps

class DefaultSinkStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  "A sink stage" should {

    "pass elements to the sink function, if no condition is specified" in {

      val function = stub[SinkFunction]
      val sinkFuture = Source(List(dataset1, dataset2))
        .toMat(new DefaultSinkStage(initialFunction = function))(Keep.right)
        .run()

      whenReady(sinkFuture) { sink =>

        function.apply _ verify dataset1
        function.apply _ verify dataset2
      }
    }
  }
}
package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.stream.ExampleData._
import io.logbee.keyscore.model.sink.SinkFunction
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

import scala.language.postfixOps
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await

@RunWith(classOf[JUnitRunner])
class DefaultSinkStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory {

  private val config = ConfigFactory.load()
  implicit val system = ActorSystem("keyscore", config.getConfig("test").withFallback(config))
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  "A sink stage" should {

    "pass elements to the sink function, if no condition is specified" in {

      val function = stub[SinkFunction]
      val sinkFuture = Source(List(dataset1, dataset2))
        .toMat(new DefaultSinkStage(initialFunction = function))(Keep.right)
        .run()


      Await.ready(sinkFuture, 10 seconds)

      function.apply _ verify dataset1
      function.apply _ verify dataset2
    }
  }
}
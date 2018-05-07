package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.model._
import io.logbee.keyscore.agent.stream.ExampleData.{vcsDatasetA, vcsDatasetB}
import io.logbee.keyscore.agent.stream.contrib.filter.CSVParserFilterFunction
import io.logbee.keyscore.agent.stream.contrib.stages.DefaultFilterStage
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._
import scala.concurrent.Await

@RunWith(classOf[JUnitRunner])
class CSVFilterFunctionSpec extends WordSpec with Matchers with ScalaFutures with MockFactory {

  private val config = ConfigFactory.load()
  implicit val system = ActorSystem("keyscore", config.getConfig("test").withFallback(config))
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  val vcsAResult = Dataset(Record(
    TextField("Philosophy" , "13"),
    TextField("Maths" , "07"),
    TextField("Latin" , "09"),
    TextField("Astrophysics" , "15")
  ))

  val vcsBResult = Dataset(Record(TextField("message", ";03;05;01;;;")))

  trait TestStream {
    val (filterFuture, probe) = Source(List(vcsDatasetA, vcsDatasetB))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A VCSFilter" should {
    "convert a vcs into a normal record" in new TestStream {
      whenReady(filterFuture) { filter =>
        val condition = stub[Condition]
        val vcsFunction = stub[CSVParserFilterFunction]

        condition.apply _ when vcsDatasetA returns Accept(vcsDatasetA)
        condition.apply _ when vcsDatasetB returns Reject(vcsDatasetB)

        vcsFunction.apply _ when vcsDatasetA returns vcsAResult
        vcsFunction.apply _ when vcsDatasetB returns vcsBResult

        Await.result(filter.changeCondition(condition), 10 seconds)
        Await.result(filter.changeFunction(vcsFunction), 10 seconds)

        probe.request(2)
        probe.expectNext(vcsAResult)
        probe.expectNext(vcsDatasetB)
      }
    }
  }

}



package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.model._
import io.logbee.keyscore.agent.stream.ExampleData.{vcsDatasetA, vcsDatasetB}
import io.logbee.keyscore.agent.stream.contrib.CSVParserFilterFunction
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
    TextField("Ident" , "CCM-160816-BJ-1"),
    TextField("Beginn Produktion" , "6.1.18 8:01"),
    TextField("Ende Produktion" , "6.1.18 8:30"),
    TextField("Temperatur" , "20,1")
  ))
  val vcsBResult = Dataset(Record(TextField("message", ";6.1.18 17:31;6.1.18 18:00;25;;;")))

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



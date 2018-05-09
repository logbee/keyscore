package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.stream.ExampleData.{csvDatasetA, csvDatasetB}
import io.logbee.keyscore.agent.stream.contrib.filter.CSVParserFilterFunction
import io.logbee.keyscore.agent.stream.contrib.stages.DefaultFilterStage
import io.logbee.keyscore.model._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class CSVFilterFunctionSpec extends WordSpec with Matchers with ScalaFutures with MockFactory {

  private val config = ConfigFactory.load()
  implicit val system = ActorSystem("keyscore", config.getConfig("test").withFallback(config))
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  val csvAResult = Dataset(Record(
    TextField("Philosophy" , "13"),
    TextField("Maths" , "07"),
    TextField("Latin" , "09"),
    TextField("Astrophysics" , "15")
  ))

  val csvBResult = Dataset(Record(TextField("message", ";03;05;01;;;")))

  trait TestStream {
    val (filterFuture, probe) = Source(List(csvDatasetA, csvDatasetB))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A CSVFilterFunction" should {
    "convert a csv string into a normal record" in new TestStream {
      whenReady(filterFuture) { filter =>
        val condition = stub[Condition]
        val csvFunction = stub[CSVParserFilterFunction]

        condition.apply _ when csvDatasetA returns Accept(csvDatasetA)
        condition.apply _ when csvDatasetB returns Reject(csvDatasetB)

        csvFunction.apply _ when csvDatasetA returns csvAResult
        csvFunction.apply _ when csvDatasetB returns csvBResult

        Await.result(filter.changeCondition(condition), 10 seconds)
        Await.result(filter.changeFunction(csvFunction), 10 seconds)

        probe.request(2)
        probe.expectNext(csvAResult)
        probe.expectNext(csvDatasetB)
      }
    }
  }

}



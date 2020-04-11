package io.logbee.keyscore.pipeline.contrib.elasticsearch

import io.logbee.keyscore.model.configuration.{Configuration, ParameterSet, TextParameter}
import io.logbee.keyscore.pipeline.testkit.TestStreamForSink
import io.logbee.keyscore.test.fixtures.{ExampleData, TestSystemWithMaterializerAndExecutionContext}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

//@RunWith(classOf[JUnitRunner])
class ElasticsearchSinkLogicSpec extends AnyWordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  implicit val defaultPatience = PatienceConfig(timeout = Span(20, Seconds), interval = Span(5, Seconds))

  "A ElasticsearchSink" should {

    val configuration = Configuration(
      parameterSet = ParameterSet(Seq(
        TextParameter("elastic.url", "http://localhost:9200"),
        TextParameter("elastic.index", "test")
      )
    ))

    "do some thing" in new TestStreamForSink[ElasticsearchSinkLogic](configuration) {

      Await.ready(sinkFuture, 20 seconds)

      source.sendNext(ExampleData.dataset1)
      source.sendNext(ExampleData.datasetMulti1)
      source.sendNext(ExampleData.dataset3)

      Thread.sleep(60000)
    }
  }
}

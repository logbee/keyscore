package io.logbee.keyscore.agent.pipeline.contrib.elasticsearch

import java.util.UUID

import akka.stream.SinkShape
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.TestSource
import io.logbee.keyscore.agent.pipeline._
import io.logbee.keyscore.agent.pipeline.stage.{LogicParameters, SinkStage, StageContext}
import io.logbee.keyscore.model.configuration.{Configuration, NumberParameter, TextParameter}
import io.logbee.keyscore.model.data.Dataset
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

//@RunWith(classOf[JUnitRunner])
class ElasticSearchSinkLogicSpec  extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  implicit val defaultPatience = PatienceConfig(timeout = Span(20, Seconds), interval = Span(5, Seconds))

  "A ElasticSearchSink" should {

    "do some thing" in {

      val configuration = Configuration(
        parameters = Seq(
          TextParameter("host","localhost"),
          NumberParameter("port", 9200),
          TextParameter("index","test")
        )
      )

      val context = StageContext(system, executionContext)
      val provider = (ctx: StageContext, c: Configuration, s: SinkShape[Dataset]) =>
        new ElasticSearchSinkLogic(LogicParameters(UUID.randomUUID(), ctx, c), s)
      val elasticSink = new SinkStage(context, configuration, provider)

      val (src, sinkFuture) = TestSource.probe[Dataset]
        .toMat(elasticSink)(Keep.both)
        .run()

      Await.ready(sinkFuture, 20 seconds)

      src.sendNext(ExampleData.dataset1)
      src.sendNext(ExampleData.datasetMulti1)
      src.sendNext(ExampleData.dataset3)

      Thread.sleep(60000)
    }
  }
}

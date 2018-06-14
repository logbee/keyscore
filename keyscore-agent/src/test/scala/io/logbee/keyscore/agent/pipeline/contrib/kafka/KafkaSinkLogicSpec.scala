package io.logbee.keyscore.agent.pipeline.contrib.kafka

import akka.stream.SinkShape
import akka.stream.scaladsl.{Keep, Source}
import io.logbee.keyscore.agent.pipeline.ExampleData.{datasetMulti1, datasetMulti2, kafkaSinkConfiguration}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{SinkStage, StageContext}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class KafkaSinkLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A KafkaSink" should {
    "push data to the specific kafka topic" in {
      val provider = (ctx: StageContext, c: FilterConfiguration, s: SinkShape[Dataset]) => new KafkaSinkLogic(ctx, c, s)

      val context = StageContext(system, executionContext)

      val sinkFuture = Source(List(datasetMulti1, datasetMulti2))
        .toMat(new SinkStage(context, kafkaSinkConfiguration, provider))(Keep.right)
        .run()

      //      whenReady(sinkFuture) { sink =>
      //
      //      }

      Thread.sleep(60000)
    }
  }

}

package io.logbee.keyscore.agent.stream

import akka.stream.SinkShape
import akka.stream.scaladsl.JavaFlowSupport.Source
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import io.logbee.keyscore.agent.stream.ExampleData.{datasetMulti1, datasetMulti2, kafkaSinkConfiguration}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration

class KafkaSinkLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A KafkaSink" should {
    "push data to the specific kafka topic" in {
      val provider = (c: FilterConfiguration, s: SinkShape[Dataset]) => new KafkaSinkLogic(c, s, system)

      val sinkFuture = Source(List(datasetMulti1, datasetMulti2))
        .toMat(new SinkStage(provider, kafkaSinkConfiguration))(Keep.right)
        .run()

      whenReady(sinkFuture) { sink =>

      }

      Thread.sleep(60000)
    }
  }

}

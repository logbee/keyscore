package io.logbee.keyscore.agent.stream

import akka.stream.SourceShape
import akka.stream.scaladsl.{Sink, Source}
import io.logbee.keyscore.model.{Condition, Dataset}
import io.logbee.keyscore.model.filter.FilterConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Promise

//@RunWith(classOf[JUnitRunner])
/**
  * IMPORTANT NOTICE:
  *
  * If you want to run this test, make sure you have a docker container from https://github.com/logbee/docker-kafka running
  * and make sure the specified "testTopic" was created and filled with:
  * {"id":"01", "name":"robo"}
  * {"id":"02", "name":"logbee"}
  * Otherwise, this test will fail
  */
class KafkaSourceLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  val updateConfiguration = Promise[FilterConfiguration]
  val initializeConfiguration = Promise[FilterConfiguration]

  "A KafkaSource" should {

    "retrieve data from a kafka source and parse it into a dataset" in {

      val provider = (c: FilterConfiguration, s: SourceShape[Dataset]) => new KafkaSourceLogic(c, s, system)

      val source = Source.fromGraph((new SourceStage(provider, ExampleData.kafkaSourceConfiguration))).runWith(Sink.ignore)

      Thread.sleep(30000)
    }
  }

}

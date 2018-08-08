package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.EventFilter
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.pipeline.ExampleData.{dataset1, messageTextField1}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class LoggerFilterSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  override implicit val system: ActorSystem = ActorSystem("testsystem", ConfigFactory.parseString(
    """akka.loggers = ["akka.testkit.TestEventListener"]"""
  ))

  val configurationA = FilterConfiguration(randomUUID(), FilterDescriptor(LoggerFilter.filterId, LoggerFilter.filterName), List.empty)
  val context = StageContext(system, executionContext)
  val filterStage = new FilterStage(context, configurationA, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new LoggerFilter(ctx, c, s))

  val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
    .viaMat(filterStage)(Keep.both)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()

  "A LoggerFilter" should {

    "return a MetaFilterDescriptor" in {
      LoggerFilter.describe should not be null
    }

    "log datasets" in {

      whenReady(filterFuture) { filter =>

        EventFilter.info(pattern = s".*${messageTextField1.toTextField.value}.*", occurrences = 1) intercept {
          source.sendNext(dataset1)

          sink.request(1)
          sink.expectNext(dataset1)
        }
      }
    }
  }
}

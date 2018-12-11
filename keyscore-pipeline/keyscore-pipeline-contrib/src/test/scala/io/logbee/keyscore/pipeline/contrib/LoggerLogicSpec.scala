package io.logbee.keyscore.pipeline.contrib

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.EventFilter
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.model.configuration.{Configuration, ParameterSet}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.test.fixtures.ExampleData.{dataset1, messageTextField1}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class LoggerLogicSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  override implicit val system: ActorSystem = ActorSystem("testsystem", ConfigFactory.parseString(
    """akka.loggers = ["akka.testkit.TestEventListener"]"""
  ))

  val configurationA = Configuration(parameterSet = ParameterSet(Seq()))
  val context = StageContext(system, executionContext)
  val provider = (parameters: LogicParameters, s: FlowShape[Dataset,Dataset]) => new LoggerLogic(parameters, s)
  val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, configurationA), provider)

  val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
    .viaMat(filterStage)(Keep.both)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()

  "A LoggerFilter" should {

    "return a MetaFilterDescriptor" in {
      LoggerLogic.describe should not be null
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

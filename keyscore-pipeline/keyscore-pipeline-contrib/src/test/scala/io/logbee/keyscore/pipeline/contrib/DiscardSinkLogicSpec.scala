package io.logbee.keyscore.pipeline.contrib

import java.lang.System.currentTimeMillis
import java.util.UUID

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSource
import akka.stream.{FlowShape, SinkShape}
import io.logbee.keyscore.model.configuration.{Configuration, NumberParameter, ParameterSet}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, SinkStage, StageContext}
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Millis, Span}
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.Promise

@RunWith(classOf[JUnitRunner])
class DiscardSinkLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A DiscardSinkLogic" - {

    "should pull elements in the specified interval" in {

      val interval = 1000L

      val configuration = Configuration(
        parameterSet = ParameterSet(Seq(
          NumberParameter(DiscardSinkLogic.intervalParameter, interval),
        ))
      )

      val context = StageContext(system, executionContext)
      val provider = (parameters: LogicParameters, s: SinkShape[Dataset]) => new DiscardSinkLogic(parameters, s)

      val durationPromise = Promise[List[Long]]

      val dummyProvider = (parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) => new FilterLogic(parameters, shape) {

        private val durations = mutable.ListBuffer.empty[Long]
        private var last =  currentTimeMillis()

        override def initialize(configuration: Configuration): Unit = {}

        override def configure(configuration: Configuration): Unit = {}

        override def onPush(): Unit = {

          push(out, grab(in))

          val current = currentTimeMillis()
          val delta = current - last

          durations += delta
          last = current
        }

        override def onPull(): Unit = {
          if (!isClosed(in)) {
            pull(in)
          }
        }

        override def onUpstreamFinish(): Unit = {
          durationPromise.success(durations.toList)
        }
      }

      val sinkStage = new SinkStage(LogicParameters(UUID.randomUUID(), context, configuration), provider)
      val dummyStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, Configuration()), dummyProvider)

      val source = Source.fromGraph(TestSource.probe[Dataset])
        .viaMat(dummyStage)(Keep.left)
        .toMat(sinkStage)(Keep.left)
        .run()

      source.sendNext(Dataset())
      source.sendNext(Dataset())
      source.sendNext(Dataset())
      source.sendComplete()

      whenReady(durationPromise.future, interval = Interval(Span(interval, Millis)), timeout = Timeout(Span(interval * 11, Millis))) { durations =>
        durations.tail.foreach(duration => {
          duration should be > interval
        })
      }
    }
  }
}

package io.logbee.keyscore.pipeline.contrib.filter

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{Configuration, NumberParameter, ParameterSet}
import io.logbee.keyscore.model.data.{Dataset, DecimalField, Field, Record, TextField, TextValue, TimestampField}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.test.fixtures.ExampleData._
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class DropOversizedRecordLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)
    val provider = (parameters: LogicParameters, s: FlowShape[Dataset,Dataset]) => new DropOversizedRecordLogic(parameters, s)
    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, configuration), provider)

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val configuration = Configuration(parameterSet = ParameterSet(Seq(
    NumberParameter(DropOversizedRecordLogic.fieldLimitParameter.ref, 3),
  )))

  val oversizedRecord = Record(
    TextField("message", "The weather is cloudy."),
    DecimalField("temperature", -11.5),
    TimestampField("timestamp", System.currentTimeMillis() / 1000),
    TextField("uuid", "f1a414a3-6122-4ba1-82d5-ee4f9c4da310")
  )

  val record = Record(
    TextField("message", "It is a sunny day."),
    DecimalField("temperature", 15.8),
  )

  "A DropOversizedRecordLogic" - {

    "should drop all oversized records" in new TestStream {

      whenReady(filterFuture) { _ =>

        source.sendNext(Dataset(oversizedRecord, record))

        sink.request(1)
        sink.expectNext(Dataset(record))
      }
    }
  }
}

package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{Configuration, NumberParameter, ParameterSet}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class DropOversizedRecordLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

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

    "should drop all oversized records" in new TestStreamForFilter[DropOversizedRecordLogic](configuration) {

      whenReady(filterFuture) { _ =>

        source.sendNext(Dataset(oversizedRecord, record))

        sink.request(1)
        sink.expectNext(Dataset(record))
      }
    }
  }
}

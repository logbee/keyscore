package io.logbee.keyscore.pipeline.contrib

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data.{Field, TextValue}
import io.logbee.keyscore.pipeline.testkit.TestStreamForSource
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ConstantSourceLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val configuration = Configuration(
    parameterSet = ParameterSet(Seq(
      FieldNameParameter(ConstantSourceLogic.fieldNameParameter.ref, "input"),
      TextListParameter(ConstantSourceLogic.inputParameter.ref, Seq(
        "Hello World", "Bye Bye"
      )),
    ))
  )

  "A ConstantSourceLogic" - {

    "should output the configured text" in new TestStreamForSource[ConstantSourceLogic](configuration){

      whenReady(sourceFuture) { _ =>

        sink.request(3)

        var record = sink.requestNext().records.head

        record.fields should contain only Field("input", TextValue("Hello World"))

        record = sink.requestNext().records.head

        record.fields should contain only Field("input", TextValue("Bye Bye"))

        record = sink.requestNext().records.head

        record.fields should contain only Field("input", TextValue("Hello World"))
      }
    }
  }
}

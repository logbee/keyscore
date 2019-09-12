package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{Configuration, FieldListParameter, ParameterSet}
import io.logbee.keyscore.model.data.{Field, TextValue}
import io.logbee.keyscore.pipeline.contrib.filter.RenameFieldsLogic.fieldsToRenameParameter
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.ExampleData._
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class RenameFieldsLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A RenameFieldsFilter" - {

    val emptyConfiguration = Configuration(parameterSet = ParameterSet(Seq(
      FieldListParameter(fieldsToRenameParameter.ref, Seq())
    )))

    val configuration2 = Configuration(parameterSet = ParameterSet(Seq(
      FieldListParameter(fieldsToRenameParameter.ref, Seq(
        Field("nameXYZ", TextValue("testValue1")),
        Field("name", TextValue("testValue2"))
      )))))

    "should return a Descriptor" in {
      RenameFieldsLogic.describe should not be null
    }

    "should pass datasets through unaltered if the list of fields to rename is empty" in new TestStreamForFilter[RenameFieldsLogic](emptyConfiguration) {

      whenReady(filterFuture) { _ =>
        source.sendNext(dataset1)
        sink.requestNext() shouldEqual dataset1
        source.sendNext(dataset5)
        sink.requestNext() shouldEqual dataset5
      }
    }

    "should replace matching names" in new TestStreamForFilter[RenameFieldsLogic](configuration2) {

      whenReady(filterFuture) { _ =>

        source.sendNext(datasetMulti1)
        sink.requestNext() shouldEqual datasetMulti1

        source.sendNext(kafkaDataset1)
        val output2 = sink.requestNext()
        output2.records.size shouldEqual 1
        output2.records(0).fields.size shouldEqual 2
        output2.records(0).fields(0).name shouldEqual kafkaDataset1.records(0).fields(0).name
        output2.records(0).fields(0).value shouldEqual kafkaDataset1.records(0).fields(0).value
        output2.records(0).fields(1).name shouldEqual "testValue2"
        output2.records(0).fields(1).value shouldEqual kafkaDataset1.records(0).fields(1).value
      }
    }
  }
}

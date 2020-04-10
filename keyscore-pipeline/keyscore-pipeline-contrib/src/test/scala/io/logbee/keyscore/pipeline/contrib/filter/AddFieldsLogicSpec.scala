package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{Configuration, FieldListParameter, ParameterSet}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.pipeline.contrib.filter.AddFieldsLogic.fieldListParameter
import io.logbee.keyscore.pipeline.testkit.{TestActorSystem, TestStreamForFilter}
import io.logbee.keyscore.test.fixtures.ExampleData.dataset1
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class AddFieldsLogicSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockFactory with TestActorSystem {

  val modified1 = Dataset(records = Record(
    Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C")),
  ))

  val modified2 = Dataset(records = Record(
    Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C")),
    Field("message3", TextValue("testValue")),
    Field("message4", TextValue("testValue2"))
  ))

  "A AddFieldsFilter" - {

    "should return a Descriptor" in {
      AddFieldsLogic.describe should not be null
    }

    val configuration = Configuration(parameterSet = ParameterSet(Seq(
      FieldListParameter(fieldListParameter.ref, Seq())
    )))

    val configuration2 = Configuration(parameterSet = ParameterSet(Seq(
      FieldListParameter(fieldListParameter.ref, Seq(
        Field("message3", TextValue("testValue")),
        Field("message4", TextValue("testValue2"))
    )))))

    "should add new fields and their data to the already existing data and change it's behaviour on configuration change" in new TestStreamForFilter[AddFieldsLogic](configuration) {

      whenReady(filterFuture) { filter =>

        source.sendNext(dataset1)

        sink.requestNext().records should contain theSameElementsAs modified1.records

        Await.ready(filter.configure(configuration2), 10 seconds)

        source.sendNext(dataset1)

        sink.requestNext().records should contain theSameElementsAs modified2.records
      }
    }
  }
}

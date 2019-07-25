package io.logbee.keyscore.example.filter

import io.logbee.keyscore.example.filter.AddFieldsLogic.fieldListParameter
import io.logbee.keyscore.model.configuration.{Configuration, FieldListParameter, ParameterSet}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.pipeline.testkit.{TestActorSystem, TestStreamForFilter}
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class AddFieldsLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestActorSystem {

  val sample = Dataset(records = Record(
    Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C")),
  ))

  "A AddFieldsFilter" - {

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

        source.sendNext(sample)

        sink.requestNext() shouldBe sample

        whenReady(filter.configure(configuration2)) { _ =>

          source.sendNext(sample)

          sink.requestNext() shouldBe Dataset(records = Record(
            Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C")),
            Field("message3", TextValue("testValue")),
            Field("message4", TextValue("testValue2"))
          ))
        }
      }
    }
  }
}

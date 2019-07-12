package io.logbee.keyscore.pipeline.contrib.filter.batch

import io.logbee.keyscore.model.configuration.{Configuration, NumberParameter}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class GroupByCountLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A GroupByCountLogic" - {

    val configuration = Configuration(
      NumberParameter("amount", 3)
    )

    val sample1 = Dataset(Record(
      Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C"))
    ))

    val sample2 = Dataset(Record(
      Field("message", TextValue("Is is a rainy day. Temperature: 5.8 C"))
    ))

    val sample3 = Dataset(Record(
      Field("message", TextValue("The weather is sunny with a current temperature of: 14.4 C")),
      Field("location", TextValue("ulm/germany"))
    ))

    "should buffer datasets until the specified amount has been reached" in new TestStreamForFilter[GroupByCountLogic](configuration){

      whenReady(filterFuture) { filter =>

        sink.request(1)
        source.sendNext(sample1)
        source.sendNext(sample2)
        sink.expectNoMessage(3 seconds)
        source.sendNext(sample3)
        sink.requestNext().records should have size 3
        source.sendNext(sample1)
        sink.expectNoMessage(3 seconds)
        source.sendNext(sample2)
        source.sendNext(sample3)
        sink.requestNext().records should have size 3
      }
    }
  }
}

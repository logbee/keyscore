package io.logbee.keyscore.pipeline.contrib.flow

import java.lang.System.currentTimeMillis

import io.logbee.keyscore.model.configuration.{ChoiceParameter, Configuration, NumberParameter}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ThrottleLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A ThrottleLogic" - {

    val sample1 = Dataset(Record(Field("message", TextValue("Hello World"))))
    val sample2 = Dataset(Record(Field("message", TextValue("The weather is sunny!"))))
    val sample3 = Dataset(Record(Field("message", TextValue("Current temperature 36.7!"))))
    val sample4 = Dataset(Record(Field("message", TextValue("Bye Bye"))))

    "when configured with 2/s" - {

      val configuration = Configuration(
        NumberParameter(ThrottleLogic.datasetsParameter, 2),
        ChoiceParameter(ThrottleLogic.timeUnitParameter, "SECOND")
      )

      "should only emit two dataset per second" in new TestStreamForFilter[ThrottleLogic](configuration) {

        var timestamp = 0L

        whenReady(filterFuture) { _ =>

          sink.request(4)
          source.sendNext(sample1)
          source.sendNext(sample2)

          timestamp = currentTimeMillis()
          sink.expectNext(2 second, sample1)
          (currentTimeMillis() - timestamp) should be (500L +- 100)

          timestamp = currentTimeMillis()
          sink.expectNext(2 second, sample2)
          (currentTimeMillis() - timestamp) should be (500L +- 100)

          Thread.sleep(1000)
          source.sendNext(sample3)
          source.sendNext(sample4)

          timestamp = currentTimeMillis()
          sink.expectNext(2 second, sample3)
          (currentTimeMillis() - timestamp) should be (0L +- 100)

          timestamp = currentTimeMillis()
          sink.expectNext(2 second, sample4)
          (currentTimeMillis() - timestamp) should be (500L +- 100)
        }
      }
    }
  }
}

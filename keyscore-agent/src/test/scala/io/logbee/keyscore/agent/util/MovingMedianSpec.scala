package io.logbee.keyscore.agent.util

import com.google.protobuf.Duration
import io.logbee.keyscore.agent.util.MovingMedian.MovingMedianItem
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class MovingMedianSpec extends WordSpec with Matchers {

  "An initialized MovingMedian" should {
    "return 0" in {
      MovingMedian().get shouldBe Duration.newBuilder().build()
    }
  }

  private val min: Int = 1
  private val max: Int = 42
  private val initMedians = Array(
    MovingMedianItem(Duration.newBuilder().setSeconds(42).build(), min + 3),
    MovingMedianItem(Duration.newBuilder().setSeconds(37).build(), min + 5),
    MovingMedianItem(Duration.newBuilder().setSeconds(12).build(), max),
    MovingMedianItem(Duration.newBuilder().setSeconds(31).build(), min),
    MovingMedianItem(Duration.newBuilder().setSeconds(75).build(), min + 7))
  private var movingMedian = MovingMedian(10)

  "An Array of MovingMedianItems" should {
    "return the minimal item" in {
      val minMedian = initMedians.min
      val oldestTime = minMedian.currentSystemTime
      oldestTime shouldBe min
    }
    "return the right index of a minimal item" in {
      initMedians.indexOf(initMedians.min) shouldBe 3
    }
    "update a new item correct" in {
      movingMedian + Duration.newBuilder().setSeconds(10).build()
      movingMedian + Duration.newBuilder().setSeconds(11).build()
      movingMedian + Duration.newBuilder().setSeconds(12).build()
      movingMedian + Duration.newBuilder().setSeconds(13).build()
      movingMedian + Duration.newBuilder().setSeconds(14).build()
      movingMedian + Duration.newBuilder().setSeconds(15).build()
      movingMedian + Duration.newBuilder().setSeconds(16).build()
      movingMedian + Duration.newBuilder().setSeconds(17).build()
      movingMedian + Duration.newBuilder().setSeconds(18).build()
      movingMedian + Duration.newBuilder().setSeconds(19).build()

      movingMedian.get shouldBe Duration.newBuilder().setSeconds(15).build()
    }
  }
}

package io.logbee.keyscore.agent.util

import io.logbee.keyscore.agent.util.MovingMedian.MovingMedianItem
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class MovingMedianSpec extends WordSpec with Matchers {

  "An initialized MovingMedian" should {
    "return 0" in {
      MovingMedian().get shouldBe 0
    }
  }

  private val min: Int = 1
  private val max: Int = 42
  private val initMedians = Array(MovingMedianItem(42, min + 3), MovingMedianItem(37, min + 5), MovingMedianItem(12, max), MovingMedianItem(31, min), MovingMedianItem(75, min + 7))
  private var movingMedian = MovingMedian(10)

  "An Array of MovingMedianItems" should {
    "return the minimal item" in {
      val minMedian = initMedians.min
      val oldestTime = minMedian.currentSystemTime
      oldestTime shouldBe (min)
    }
    "return the right index of a minimal item" in {
      initMedians.indexOf(initMedians.min) shouldBe (3)
    }
    "update a new item correct" in {
      movingMedian + 10
      movingMedian + 11
      movingMedian + 12
      movingMedian + 13
      movingMedian + 14
      movingMedian + 15
      movingMedian + 16
      movingMedian + 17
      movingMedian + 18
      movingMedian + 19
      Thread.sleep(1000)
      movingMedian.get shouldBe (15)
    }
  }
}

package io.logbee.keyscore.agent.util

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
}

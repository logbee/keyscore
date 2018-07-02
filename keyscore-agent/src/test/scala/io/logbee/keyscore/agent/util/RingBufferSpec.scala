package io.logbee.keyscore.agent.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RingBufferSpec extends WordSpec with Matchers {


  "An empty RingBuffer" should {

    val ringBuffer = RingBuffer[Int](5)

    "not be filled" in {
      ringBuffer.isNonEmpty shouldBe false
      ringBuffer.isFull shouldBe false
    }

    "not be nonEmpty" in {
      ringBuffer.push(1)
      ringBuffer.push(2)

      ringBuffer.isNonEmpty shouldBe true
    }

    "isNotFull" in {
      ringBuffer.push(1)
      ringBuffer.isNotFull shouldBe true
    }

    "isFull" in {
      ringBuffer.push(1)
      ringBuffer.push(2)
      ringBuffer.push(3)
      ringBuffer.push(4)
      ringBuffer.push(5)
      ringBuffer.isFull shouldBe true
    }
  }

  "A RingBuffer with size 3" should {
    val ringBuffer = RingBuffer[Int](3)
    "write only 3 elements" in {
      ringBuffer.push(1)
      ringBuffer.push(2)
      ringBuffer.push(3)
      ringBuffer.push(4)

      ringBuffer.pull() shouldBe 4
      ringBuffer.pull() shouldBe 2
      ringBuffer.pull() shouldBe 3

    }
    "pull the right element" in {
      ringBuffer.clear()
      ringBuffer.push(1)
      ringBuffer.pull() shouldBe 1
    }
  }

  "A RingBuffer filled with 3 elements" should {
    val ringBuffer = RingBuffer[Int](3)
    ringBuffer.push(1)
    ringBuffer.push(2)
    ringBuffer.push(3)

    "return 3 elements on take" in {
      ringBuffer.take(3) should contain inOrderOnly (3, 2, 1)
    }

    "return 3 elements if 4 are requested" in {
      ringBuffer.take(4) should contain inOrderOnly (3, 2, 1)
    }

    "return elements with shifted writepointer" in  {
      ringBuffer.pull()
      ringBuffer.take(3) should contain inOrderOnly (3, 2)
    }

    "take single element of Array with one element" in {
      val testBuffer = RingBuffer[Int](5)

      testBuffer.push(1)

      testBuffer.take(1) should contain (1)
    }
  }

  "A RingBuffer should return the last n elements" in {
    val ringBuffer = RingBuffer[Int](3)
    ringBuffer.push(1)
    ringBuffer.push(2)
    ringBuffer.push(3)
    ringBuffer.push(4)

    ringBuffer.pull()
    ringBuffer.pull()
    ringBuffer.pull()

    ringBuffer.last(4) should contain inOrderOnly(4, 3, 2)
  }
}

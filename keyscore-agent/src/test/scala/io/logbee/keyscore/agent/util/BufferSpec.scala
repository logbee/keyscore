package io.logbee.keyscore.agent.util

import org.scalatest.{Matchers, WordSpec}

class BufferSpec extends WordSpec with Matchers {


  "An empty Buffer" should {

    val ringBuffer = Buffer[Int](5)

    "not be filled" in {
      ringBuffer.isNonEmpty() shouldBe false
      ringBuffer.isFull() shouldBe false
    }

    "not be nonEmpty" in {
      ringBuffer.push(1)
      ringBuffer.push(2)

      ringBuffer.isNonEmpty() shouldBe true
    }

    "isNotFull" in {
      ringBuffer.push(1)
      ringBuffer.isNotFull() shouldBe true
    }

    "isFull" in {
      ringBuffer.push(1)
      ringBuffer.push(2)
      ringBuffer.push(3)
      ringBuffer.push(4)
      ringBuffer.push(5)
      ringBuffer.isFull() shouldBe true
    }
  }

  "A Buffer with size 3" should {
    val ringBuffer = Buffer[Int](3)
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

  "A Buffer filled with 3 elements" should {
    val ringBuffer = Buffer[Int](3)
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
      val testBuffer = Buffer[Int](5)

      testBuffer.push(1)

      testBuffer.take(1) should contain (1)
    }

  }

}

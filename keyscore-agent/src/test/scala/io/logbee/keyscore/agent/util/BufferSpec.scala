package io.logbee.keyscore.agent.util

import org.scalatest.{Matchers, WordSpec}

class BufferSpec extends WordSpec with Matchers {


  "A empty Buffer" should {

    val ringBuffer = Buffer[Int](5)

    "not be filled" in {
      ringBuffer.isFull() shouldBe true
    }

    "not be nonEmpty" in {
      ringBuffer.push(1)
      ringBuffer.push(2)
      ringBuffer.push(3)
      ringBuffer.push(4)
      ringBuffer.push(5)
      ringBuffer.isEmpty() shouldBe true
    }
  }

  "A Buffer with size 3" should {
    val ringBuffer = Buffer[Int](3)
    "should write only 3 elements" in {
      ringBuffer.push(1)
      ringBuffer.push(2)
      ringBuffer.push(3)
      ringBuffer.push(4)

      ringBuffer.pull() shouldBe 2
      ringBuffer.pull() shouldBe 3
      ringBuffer.pull() shouldBe 4
    }
  }

}

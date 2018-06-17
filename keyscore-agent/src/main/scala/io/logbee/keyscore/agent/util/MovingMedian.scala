package io.logbee.keyscore.agent.util


object MovingMedian {
  def apply(window: Int = 10): MovingMedian = new MovingMedian(window)

  implicit def median2Long(median: MovingMedian): Long = {
    median.get
  }
}

class MovingMedian(window: Int) {

  def +(value: Long): MovingMedian = {
    this
  }

  def get: Long = {
      0
  }

  def reset(): Unit = {

  }
}

package io.logbee.keyscore.agent.util

import io.logbee.keyscore.agent.util.MovingMedian.MovingMedianItem


object MovingMedian {
  def apply(window: Int = 10): MovingMedian = new MovingMedian(window)

  implicit def median2Long(median: MovingMedian): Long = {
    median.get
  }

  case class MovingMedianItem(throughputTime: Long, currentSystemTime: Long = System.currentTimeMillis()) extends Ordered[MovingMedianItem] {
    override def compare(that: MovingMedianItem): Int = {
      this.currentSystemTime compare that.currentSystemTime
    }
  }
}

class MovingMedian(window: Int) {
  var isEmpty: Boolean = true
  var medians: Array[MovingMedianItem] = Array.ofDim(window)

  def +(value: Long): MovingMedian = {
    isEmpty = false
    val item = MovingMedianItem(value)
    var indexToUpdate = 0
    val itemsCount = numberOfItems
    if (itemsCount < window) {
      indexToUpdate = itemsCount
    } else {
      indexToUpdate = oldestIndex
    }
    medians.update(indexToUpdate, item)
    this
  }

  def get: Long = {
    var time =  0L
    if (!isEmpty) {
      val medianItem = medians(numberOfItems / 2)
      time = medianItem.throughputTime
      time
    } else 0

  }

  def reset(): Unit = {
    medians = Array.empty
  }

  private def numberOfItems: Int = {
    medians.foldLeft(0)((number, item) => if (item.isInstanceOf[MovingMedianItem]) number + 1 else number)
  }

  private def oldestIndex: Int = {
    medians.indexOf(medians.min)
  }

}

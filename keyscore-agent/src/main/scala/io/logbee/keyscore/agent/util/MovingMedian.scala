package io.logbee.keyscore.agent.util

import io.logbee.keyscore.agent.util.MovingMedian.MovingMedianItem


object MovingMedian {
  def apply(window: Int = 10): MovingMedian = new MovingMedian(window)

  implicit def median2Long(median: MovingMedian): Long = {
    median.get
  }

  case class MovingMedianItem(value: Long, currentSystemTime: Long = System.currentTimeMillis()) extends Ordered[MovingMedianItem] {
    override def compare(that: MovingMedianItem): Int = {
      this.currentSystemTime compare that.currentSystemTime
    }
  }
}

class MovingMedian(window: Int) {
  var medians: Array[MovingMedianItem] = Array.ofDim(window)
  var isEmpty: Boolean = true

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
    var value = 0L
    val itemsCount = numberOfItems
    if (itemsCount > 0) {
      val medianItem = medians(itemsCount / 2)
      value = medianItem.value
    }
    value

  }

  def reset(): Unit = {
    var emptyArray: Array[MovingMedianItem] = Array.ofDim(window)
    medians = emptyArray
  }

  private def numberOfItems: Int = {
    medians.foldLeft(0)((number, item) => if (item.isInstanceOf[MovingMedianItem]) number + 1 else number)
  }

  private def oldestIndex: Int = {
    medians.indexOf(medians.min)
  }

}

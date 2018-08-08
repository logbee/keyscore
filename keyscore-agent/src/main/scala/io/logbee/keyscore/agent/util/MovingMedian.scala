package io.logbee.keyscore.agent.util

import com.google.protobuf.Duration
import io.logbee.keyscore.agent.util.MovingMedian.MovingMedianItem


object MovingMedian {
  def apply(window: Int = 10): MovingMedian = new MovingMedian(window)

  implicit def median2Duration(median: MovingMedian): Duration = {
    median.get
  }

  case class MovingMedianItem(value: Duration, currentSystemTime: Long = System.currentTimeMillis()) extends Ordered[MovingMedianItem] {
    override def compare(that: MovingMedianItem): Int = {
      this.currentSystemTime compare that.currentSystemTime
    }
  }
}

class MovingMedian(window: Int) {
  var medians: Array[MovingMedianItem] = Array.ofDim(window)
  var isEmpty: Boolean = true

  def +(value: Duration): MovingMedian = {
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

  def get: Duration = {
    val itemsCount = numberOfItems
    if (itemsCount > 0) {
      val medianItem = medians(itemsCount / 2)
      return medianItem.value
    }
    Duration.newBuilder().build()
  }

  def reset(): Unit = {
    medians = Array.ofDim(window)
  }

  private def numberOfItems: Int = {
    medians.foldLeft(0)((number, item) => if (item.isInstanceOf[MovingMedianItem]) number + 1 else number)
  }

  private def oldestIndex: Int = {
    medians.indexOf(medians.min)
  }

  override def toString = s"MovingMedian($get)"
}

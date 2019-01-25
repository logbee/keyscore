package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File

import scala.collection.mutable.Queue


case class ReadScheduleItem(baseFile: File, startPos: Long, endPos: Long, lastModified: Long)


class ReadSchedule() {
  
  private val readScheduleQueue = Queue[ReadScheduleItem]()
  
  
  def queue(readScheduleItem: ReadScheduleItem) = {
    readScheduleQueue.enqueue(readScheduleItem)
  }
  
  
  def removeNext() = {
    if (readScheduleQueue.isEmpty)
      None
    else
      Some(readScheduleQueue.dequeue())
  }
}

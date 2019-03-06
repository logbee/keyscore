package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File
import scala.collection.mutable.Queue


case class ReadScheduleItem(baseFile: File, startPos: Long, endPos: Long, lastModified: Long, newerFilesWithSharedLastModified: Int)


class ReadSchedule() {
  private var readScheduleQueue = Queue[ReadScheduleItem]()
  
  
  def enqueue(readScheduleItem: ReadScheduleItem) = {
    readScheduleQueue.enqueue(readScheduleItem)
  }
  
  
  def dequeue(): Option[ReadScheduleItem] = {
    if (readScheduleQueue.isEmpty) {
      None
    }
    else {
      Some(readScheduleQueue.dequeue())
    }
  }
}

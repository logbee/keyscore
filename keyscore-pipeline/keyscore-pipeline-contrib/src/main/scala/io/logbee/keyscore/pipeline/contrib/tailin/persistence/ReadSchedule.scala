package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import scala.collection.mutable.Queue


case class ReadScheduleItem(file: File, startPos: Long, endPos: Long, lastModified: Long)


class ReadSchedule() {
  
  private val readScheduleQueue = Queue[ReadScheduleItem]()
  
  
  def queue(readScheduleItem: ReadScheduleItem) = {
    readScheduleQueue.enqueue(readScheduleItem)
  }
  
  
  
  def getNext: Option[ReadScheduleItem] = {
    if (readScheduleQueue.isEmpty)
      None
    else
      Some(readScheduleQueue.head)
  }
  
  
  def removeNext() = {
    if (readScheduleQueue.isEmpty)
      None
    else
      Some(readScheduleQueue.dequeue())
  }
}

package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File

import scala.collection.mutable.Queue
import scala.collection.mutable.Stack


case class ReadScheduleItem(baseFile: File, startPos: Long, endPos: Long, writeTimestamp: Long)


class ReadSchedule() {
  
  private val readScheduleStack = Stack[ReadScheduleItem]()
  
  
  def push(readScheduleItem: ReadScheduleItem) = {
    readScheduleStack.push(readScheduleItem)
  }
  
  
  def pop() = {
    if (readScheduleStack.isEmpty)
      None
    else
      Some(readScheduleStack.pop())
  }
}

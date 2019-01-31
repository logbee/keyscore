package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File


case class ReadScheduleItem(baseFile: File, startPos: Long, endPos: Long, writeTimestamp: Long)


class ReadSchedule() {
  private var readScheduleList = List[ReadScheduleItem]()
  
  
  def push(readScheduleItem: ReadScheduleItem) = {
    readScheduleList = readScheduleList :+ readScheduleItem
  }
  
  
  def pop(): Option[ReadScheduleItem] = {
    if (readScheduleList.isEmpty) {
      None
    }
    else {
      val returnVal = readScheduleList.last
      
      readScheduleList = readScheduleList.dropRight(1)
      
      Some(returnVal)
    }
  }
}

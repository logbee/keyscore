package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule


class FileReaderManager(fileReaderProvider: FileReaderProvider, readSchedule: ReadSchedule, readPersistence: ReadPersistence) {
  
  private val fileReaders = Map[File, FileReader]()
  
  private def getFileReader(fileToRead: File): FileReader = {
    var fileReaderOpt = fileReaders.get(fileToRead)
    
    var fileReader: FileReader = null
    if (fileReaderOpt == Some) {
      fileReader = fileReaderOpt.get
    }
    else { //create a new fileReader, if there's not yet one in the map
      fileReader = fileReaderProvider.create(fileToRead)
      fileReaders + (fileToRead -> fileReader)
    }
    
    fileReader
  }
  
  
  def getNextString(callback: FileReadData => Unit) = {
    
    //dequeue the next schedule entry
    val readScheduleItemOpt = readSchedule.pop()
    
    readScheduleItemOpt match {
      case None => //if no reads scheduled
        //do nothing, rescheduling is done by caller
        
      case Some(readScheduleItem) =>
        
        val fileToRead = readScheduleItem.file
        
        val completedRead = readPersistence.getCompletedRead(fileToRead)
        
        if (readScheduleItem.writeTimestamp == fileToRead.lastModified)
//        if (readScheduleItem.writeTimestamp > completedRead.previousReadTimestamp)
          getFileReader(fileToRead).read(callback, readScheduleItem)
        //else the item is invalid, ignore it (has been removed from the stack already)
    }
  }
}

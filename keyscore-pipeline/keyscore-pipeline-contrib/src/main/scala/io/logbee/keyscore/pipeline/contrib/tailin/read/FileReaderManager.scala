package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper


class FileReaderManager(fileReaderProvider: FileReaderProvider, readSchedule: ReadSchedule, readPersistence: ReadPersistence, rotationPattern: String) {
  
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
    val readScheduleItemOpt = readSchedule.dequeue()
    
    readScheduleItemOpt match {
      case None => //if no reads scheduled
        //do nothing, rescheduling is done by caller
        
      case Some(readScheduleItem) =>
        
        val baseFile = readScheduleItem.baseFile
        
        val completedRead = readPersistence.getCompletedRead(baseFile)
        
        
        val fileToRead = RotationHelper.getFilesToRead(baseFile, rotationPattern, completedRead.previousReadTimestamp).head
        
        getFileReader(fileToRead).read(callback, readScheduleItem)
    }
  }
}

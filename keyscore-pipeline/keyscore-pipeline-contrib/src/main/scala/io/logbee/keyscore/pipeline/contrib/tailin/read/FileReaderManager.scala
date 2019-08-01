package io.logbee.keyscore.pipeline.contrib.tailin.read

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper


class FileReaderManager(fileReaderProvider: FileReaderProvider, readSchedule: ReadSchedule, readPersistence: ReadPersistence, rotationPattern: String) {
  
  private val fileReaders = Map[FileHandle, FileReader]()
  
  private def getFileReader(fileToRead: FileHandle): FileReader = {
    var fileReaderOpt = fileReaders.get(fileToRead)
    
    var fileReader: FileReader = null
    if (fileReaderOpt.isDefined) {
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
        //do nothing, rescheduling is triggered by caller
        
      case Some(readScheduleItem) =>
        
        val baseFile = readScheduleItem.baseFile
        
        val completedRead = readPersistence.getCompletedRead(baseFile)
        
        
        val fileToRead = RotationHelper.getRotationFilesToRead(baseFile, rotationPattern, completedRead).head
        
        
        val callback2: FileReadData => Unit =
          fileReadData => {
            callback(fileReadData.copy(baseFile=baseFile)) //fileReader doesn't know what the baseFile is
          }
        
        getFileReader(fileToRead).read(callback2, readScheduleItem)
    }
  }
}

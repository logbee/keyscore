package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader


class ReadScheduler(baseFile: File, rotationPattern: String, persistenceContext: PersistenceContext, readSchedule: ReadSchedule) extends FileWatcher {
  
  var previouslyScheduled = FileReadRecord(previousReadPosition=0, previousReadTimestamp=0)
  
  val persistenceContextEntryOption = persistenceContext.load[FileReadRecord](baseFile.getAbsolutePath)
  if (persistenceContextEntryOption != None) {
    val persistenceContextEntry = persistenceContextEntryOption.get
    
    previouslyScheduled = FileReadRecord(previousReadPosition=persistenceContextEntry.previousReadPosition,
                                        previousReadTimestamp=persistenceContextEntry.previousReadTimestamp)
  }
  
  
  def fileModified(): Unit = {

    val filesToRead = FileReader.getFilesToRead(baseFile, rotationPattern, previouslyScheduled.previousReadTimestamp)
    
    
    var startPos = previouslyScheduled.previousReadPosition
    
    filesToRead.foreach{ fileToRead =>
      
      if (startPos != fileToRead.length) { //getFilesToRead returns files which have lastModified == previousReadTimestamp (which we would technically not need to read, but helps simplify this situation)
        val endPos = fileToRead.length
        val timestamp = fileToRead.lastModified
        
        readSchedule.queue(ReadScheduleItem(baseFile, fileToRead, startPos, endPos, timestamp))
        previouslyScheduled = FileReadRecord(endPos, timestamp)
      }
      startPos = 0 //if there's multiple files, read the next files from the start
    }
  }
  
  
  def pathDeleted(): Unit = {} //TODO delete entries from file or somehow tell fileReader to teardown (FileReader should probably finish reading out rotated files, if those haven't been deleted yet, before it does so)
                               //in general, FileReader should have a mechanism of dealing with missing files, too.
  
  def tearDown(): Unit = {}
}
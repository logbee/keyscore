package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem


class ReadScheduler(file: File, rotationPattern: String, persistenceContext: PersistenceContext, readSchedule: ReadSchedule) extends FileWatcher {
  
  var previouslyScheduled = FileReadRecord(previousReadPosition=0, previousReadTimestamp=0)
  
  val persistenceContextEntryOption = persistenceContext.load[FileReadRecord](file.getAbsolutePath)
  if (persistenceContextEntryOption != None) {
    val persistenceContextEntry = persistenceContextEntryOption.get
    
    previouslyScheduled = FileReadRecord(previousReadPosition=persistenceContextEntry.previousReadPosition,
                                        previousReadTimestamp=persistenceContextEntry.previousReadTimestamp)
  }
  
  
  def fileModified(callback: String => Unit): Unit = { //TODO do we want the callback here? We could give it readSchedule.queue as callback -> we still need access to the readSchedule to determine the last-scheduled entry, so this doesn't help much

    val filesToRead = FileReader.getFilesToRead(file, rotationPattern, previouslyScheduled.previousReadTimestamp)
    
    
    var startPos = previouslyScheduled.previousReadPosition
    
    filesToRead.foreach{ fileToRead =>
      
      if (startPos != fileToRead.length) { //getFilesToRead returns files which have lastModified == previousReadTimestamp (which we would technically not need to read, but helps simplify this situation)
        val endPos = fileToRead.length
        val timestamp = fileToRead.lastModified
        
        readSchedule.queue(ReadScheduleItem(file, startPos, endPos, timestamp))
        previouslyScheduled = FileReadRecord(endPos, timestamp)
      }
      startPos = 0 //if there's multiple files, read the next files from the start
    }
  }
  
  
  def pathDeleted(): Unit = {} //TODO delete entries from file or somehow tell fileReader to teardown (FileReader should probably finish reading out rotated files, if those haven't been deleted yet, before it does so)
                               //in general, FileReader should have a mechanism of dealing with missing files, too.
  
  def tearDown(): Unit = {}
}
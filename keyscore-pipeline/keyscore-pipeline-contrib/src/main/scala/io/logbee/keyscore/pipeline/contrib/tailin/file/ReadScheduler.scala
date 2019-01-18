package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem


class ReadScheduler(file: File, rotationPattern: String, persistenceContext: PersistenceContext, readSchedule: ReadSchedule) extends FileWatcher {
  //TODO make a generic version of readSchedule, which doesn't write it into a file
  
  
  
  /**
   * This method finds out from what file position onwards the next read has to be scheduled.
   * 
   * For that it first checks the currently scheduled reads in the readSchedule,
   * and takes the latest scheduled read's end position.
   * 
   * If no reads have been scheduled, it gets the end position of the last completed read in the persistenceContext.
   * 
   * If no reads have been scheduled or completed, it returns a position of 0 (which means to read from the start).
   * 
   * In order to support rotated files, we also return the timestamp of this last read.
   * (In the group of rotated files + main-file, which have been modified after our last read,
   * the least recently modified file should be the file where we have to continue reading at the determined position.)
   * 
   * @return The ending position and timestamp of the last scheduled or completed read.
   * Or 0 for both values, if no read has been scheduled or completed yet.
   */
  private def getContinue: FileReadRecord = {
    
    val latestFileReadScheduleItem_Option = readSchedule.getLatestEntry(file)
    
    if (latestFileReadScheduleItem_Option != None) {
      val latestFileReadScheduleItem = latestFileReadScheduleItem_Option.get
      
      FileReadRecord(previousReadPosition=latestFileReadScheduleItem.endPos,
                     previousReadTimestamp=latestFileReadScheduleItem.lastModified)
    }
    else {
      val persistenceContextEntryOption = persistenceContext.load[FileReadRecord](file.getAbsolutePath)
      
      if (persistenceContextEntryOption != None) {
        val persistenceContextEntry = persistenceContextEntryOption.get
        
        FileReadRecord(previousReadPosition=persistenceContextEntry.previousReadPosition,
                       previousReadTimestamp=persistenceContextEntry.previousReadTimestamp)
      }
      else {
        FileReadRecord(previousReadPosition=0, previousReadTimestamp=0)
      }
    }
  }
  
  
  def fileModified(callback: String => Unit): Unit = { //TODO do we want the callback here? We could give it readSchedule.queue as callback -> we still need access to the readSchedule to determine the last-scheduled entry, so this doesn't help much

    val continue = getContinue
    
    val filesToRead = FileReader.getFilesToRead(file, rotationPattern, continue.previousReadTimestamp)
    
    
    var startPos = continue.previousReadPosition
    
    filesToRead.foreach{ fileToRead =>
      
      if (startPos != fileToRead.length) { //getFilesToRead returns files which have lastModified == previousReadTimestamp (which we would technically not need to read, but helps simplify this situation)
        val readScheduleItem = ReadScheduleItem(file, startPos, fileToRead.length, fileToRead.lastModified)
        readSchedule.queue(readScheduleItem)
      }
      startPos = 0 //if there's multiple files, read the next files from the start
    }
    
    //TODO
    //-> we should sort the entries in readSchedule
    //-> probably makes most sense to traverse it backwards and compare the lastModified time
  }
  
  
  def pathDeleted(): Unit = {}
  
  def tearDown(): Unit = {}
}
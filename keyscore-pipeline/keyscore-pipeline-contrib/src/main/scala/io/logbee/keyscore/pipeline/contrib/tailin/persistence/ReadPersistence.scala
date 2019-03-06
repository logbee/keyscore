package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File

import scala.reflect.runtime.universe.typeTag

import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord


class ReadPersistence(completedPersistence: PersistenceContext, committedPersistence: PersistenceContext) {
  
  
  def getCompletedRead(baseFile: File): FileReadRecord = {
    
    var nextRead = FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0)
    
    val readPersistenceContextEntryOpt = completedPersistence.load[FileReadRecord](baseFile.getAbsolutePath)
    if (readPersistenceContextEntryOpt != None) {
      val readPersistenceContextEntry = readPersistenceContextEntryOpt.get
      
      nextRead = FileReadRecord(previousReadPosition=readPersistenceContextEntry.previousReadPosition,
                                previousReadTimestamp=readPersistenceContextEntry.previousReadTimestamp,
                                newerFilesWithSharedLastModified=readPersistenceContextEntry.newerFilesWithSharedLastModified)
    }
    else { //if no completed, uncommitted reads have been persisted
      val commitPersistenceContextEntryOpt = committedPersistence.load[FileReadRecord](baseFile.getAbsolutePath)
      if (commitPersistenceContextEntryOpt != None) {
        val commitPersistenceContextEntry = commitPersistenceContextEntryOpt.get
        
        nextRead = FileReadRecord(previousReadPosition=commitPersistenceContextEntry.previousReadPosition,
                                  previousReadTimestamp=commitPersistenceContextEntry.previousReadTimestamp,
                                  newerFilesWithSharedLastModified=commitPersistenceContextEntry.newerFilesWithSharedLastModified)
      }
    }
    
    nextRead
  }
  
  
  def completeRead(baseFile: File, fileReadRecord: FileReadRecord) = {
    
    val readPersistenceEntryOpt = completedPersistence.load[FileReadRecord](baseFile.getAbsolutePath)(typeTag[FileReadRecord])
    readPersistenceEntryOpt match {
      case Some(readPersistenceEntry) =>
        if (readPersistenceEntry.previousReadTimestamp < fileReadRecord.previousReadTimestamp) {
          completedPersistence.store(baseFile.getAbsolutePath, fileReadRecord)
        }
        
      case None =>
        completedPersistence.store(baseFile.getAbsolutePath, fileReadRecord)
    }
  }
  
  
  def commitRead(baseFile: File, fileReadRecord: FileReadRecord) = {
    
    { //check if the timestamp to commit is for some reason newer than the timestamp of the last completed read
      val readPersistenceEntryOpt = completedPersistence.load[FileReadRecord](baseFile.getAbsolutePath)(typeTag[FileReadRecord])
      readPersistenceEntryOpt match {
        case Some(readPersistenceEntry) =>
          if (readPersistenceEntry.previousReadTimestamp < fileReadRecord.previousReadTimestamp) {
            completedPersistence.store(baseFile.getAbsolutePath, fileReadRecord)
          }
          
        case None =>
          completedPersistence.store(baseFile.getAbsolutePath, fileReadRecord)
      }
    }
    
    
    //make sure the entry to commit is actually newer than the entry we currently have committed
    val commitPersistenceEntryOpt = committedPersistence.load[FileReadRecord](baseFile.getAbsolutePath)(typeTag[FileReadRecord])
    commitPersistenceEntryOpt match {
      case Some(commitPersistenceEntry) =>
        if (commitPersistenceEntry.previousReadTimestamp < fileReadRecord.previousReadTimestamp) {
          committedPersistence.store(baseFile.getAbsolutePath, fileReadRecord)
        }
        
      case None =>
        committedPersistence.store(baseFile.getAbsolutePath, fileReadRecord)
    }
  }
}

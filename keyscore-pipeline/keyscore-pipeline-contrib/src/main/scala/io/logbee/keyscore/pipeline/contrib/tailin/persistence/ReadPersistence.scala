package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import scala.reflect.runtime.universe.typeTag

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord


class ReadPersistence(completedPersistence: PersistenceContext, committedPersistence: PersistenceContext) {
  
  
  def getCompletedRead(baseFile: FileHandle): FileReadRecord = {
    
    completedPersistence.load[FileReadRecord](baseFile.absolutePath) match {
      case Some(readPersistenceContextEntry) => readPersistenceContextEntry
        
      case None => //if no completed, uncommitted reads have been persisted
        committedPersistence.load[FileReadRecord](baseFile.absolutePath) match {
          case Some(commitPersistenceContextEntry) => commitPersistenceContextEntry
            
          case None => //if nothing is found in neither the completed nor the committed entries
            FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0)
        }
    }
  }
  
  
  def completeRead(baseFile: FileHandle, fileReadRecord: FileReadRecord): Unit = {
    
    completedPersistence.load[FileReadRecord](baseFile.absolutePath)(typeTag[FileReadRecord]) match {
      case Some(readPersistenceEntry) =>
        if (readPersistenceEntry.previousReadTimestamp < fileReadRecord.previousReadTimestamp) {
          completedPersistence.store(baseFile.absolutePath, fileReadRecord)
        }
        
      case None =>
        completedPersistence.store(baseFile.absolutePath, fileReadRecord)
    }
  }
  
  
  def commitRead(baseFile: FileHandle, fileReadRecord: FileReadRecord): Unit = {
    
    { //check if the timestamp to commit is for some reason newer than the timestamp of the last completed read
      completedPersistence.load[FileReadRecord](baseFile.absolutePath)(typeTag[FileReadRecord]) match {
        case Some(readPersistenceEntry) =>
          if (readPersistenceEntry.previousReadTimestamp < fileReadRecord.previousReadTimestamp) {
            completedPersistence.store(baseFile.absolutePath, fileReadRecord)
          }
          
        case None =>
          completedPersistence.store(baseFile.absolutePath, fileReadRecord)
      }
    }
    
    
    //make sure the entry to commit is actually newer than the entry we currently have committed
    committedPersistence.load[FileReadRecord](baseFile.absolutePath)(typeTag[FileReadRecord]) match {
      case Some(commitPersistenceEntry) =>
        if (commitPersistenceEntry.previousReadTimestamp < fileReadRecord.previousReadTimestamp) {
          committedPersistence.store(baseFile.absolutePath, fileReadRecord)
        }
        
      case None =>
        committedPersistence.store(baseFile.absolutePath, fileReadRecord)
    }
  }
}

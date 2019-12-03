package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord

import scala.reflect.runtime.universe.typeTag


class ReadPersistence(completedPersistence: PersistenceContext[String, FileReadRecord], committedPersistence: PersistenceContext[String, FileReadRecord]) {

  def getCompletedRead(baseFile: FileHandle): FileReadRecord = {
    val absolutePath = baseFile.absolutePath

    completedPersistence.load(absolutePath) match {
      case Some(readPersistenceContextEntry) => readPersistenceContextEntry
        
      case None => //if no completed, uncommitted reads have been persisted
        committedPersistence.load(absolutePath) match {
          case Some(commitPersistenceContextEntry) => commitPersistenceContextEntry
            
          case None => //if nothing is found in neither the completed nor the committed entries
            FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0)
        }
    }
  }
  
  
  def completeRead(baseFile: FileHandle, fileReadRecord: FileReadRecord): Unit = {
    val absolutePath = baseFile.absolutePath

    completedPersistence.load(absolutePath) match {
      case Some(readPersistenceEntry) =>
        if (readPersistenceEntry.previousReadTimestamp <= fileReadRecord.previousReadTimestamp) {
          completedPersistence.store(absolutePath, fileReadRecord)
        }
        
      case None =>
        completedPersistence.store(absolutePath, fileReadRecord)
    }
  }
  
  
  def commitRead(baseFile: FileHandle, fileReadRecord: FileReadRecord): Unit = {

    val absolutePath = baseFile.absolutePath

    { //check if the timestamp to commit is for some reason newer than the timestamp of the last completed read
      completedPersistence.load(absolutePath) match {
        case Some(readPersistenceEntry) =>
          if (readPersistenceEntry.previousReadTimestamp <= fileReadRecord.previousReadTimestamp) {
            completedPersistence.store(absolutePath, fileReadRecord)
          }
          
        case None =>
          completedPersistence.store(absolutePath, fileReadRecord)
      }
    }
    
    
    //make sure the entry to commit is actually newer than the entry we currently have committed
    committedPersistence.load(absolutePath) match {
      case Some(commitPersistenceEntry) =>
        if (commitPersistenceEntry.previousReadTimestamp <= fileReadRecord.previousReadTimestamp) {
          committedPersistence.store(absolutePath, fileReadRecord)
        }
        
      case None =>
        committedPersistence.store(absolutePath, fileReadRecord)
    }
  }
}

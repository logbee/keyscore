package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.io.File

import scala.collection.mutable.Queue

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence


case class FileReadData(string: String, baseFile: File, readEndPos: Long, writeTimestamp: Long)


class SendBuffer(fileReaderManager: FileReaderManager, readPersistence: ReadPersistence) {
  private var buffer: Queue[FileReadData] = Queue.empty
  
  def addToBuffer(fileReadData: FileReadData) = {
    buffer.enqueue(fileReadData)
  }
  
  def getNextElement: FileReadData = {
    
    ensureFilledIfPossible()
    
    buffer.dequeue()
  }
  
  def isEmpty: Boolean = {
    
    ensureFilledIfPossible()
    
    buffer.isEmpty
  }
  
  
  private def ensureFilledIfPossible() = {
    
    if (buffer.size <= 1) { //TODO make this asynchronous, with enough buffer size to not likely run into delays
      fileReaderManager.getNextString(fileReadData => {
        buffer.enqueue(fileReadData)
        readPersistence.completeRead(fileReadData.baseFile, FileReadRecord(fileReadData.readEndPos, fileReadData.writeTimestamp))
      })
    }
  }
}
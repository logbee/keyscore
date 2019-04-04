package io.logbee.keyscore.pipeline.contrib.tailin.read

import scala.collection.mutable.Queue

import io.logbee.keyscore.pipeline.contrib.tailin.file.File
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence


case class FileReadData(string: String,
                        baseFile: File,
                        physicalFile: String,
                        readEndPos: Long,
                        writeTimestamp: Long,
                        readTimestamp: Long,
                        newerFilesWithSharedLastModified: Int)


class SendBuffer(fileReaderManager: FileReaderManager, readPersistence: ReadPersistence) {
  private var buffer: Queue[FileReadData] = Queue.empty
  
  
  def getNextElement: Option[FileReadData] = {
    
    ensureFilledIfPossible()
    
    if (buffer.isEmpty) {
      None
    }
    else {
      Some(buffer.dequeue())
    }
  }
  
  
  def isEmpty: Boolean = {
    
    ensureFilledIfPossible()
    
    buffer.isEmpty
  }
  
  
  private def ensureFilledIfPossible() = {
    
    if (buffer.size <= 1) { //TODO make this asynchronous, with enough buffer size to not likely run into delays
      fileReaderManager.getNextString(fileReadData => {
        buffer.enqueue(fileReadData)
        readPersistence.completeRead(fileReadData.baseFile, FileReadRecord(fileReadData.readEndPos, fileReadData.writeTimestamp, newerFilesWithSharedLastModified=fileReadData.newerFilesWithSharedLastModified))
      })
    }
  }
}

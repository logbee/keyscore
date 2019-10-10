package io.logbee.keyscore.pipeline.contrib.tailin.read

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader.FileReadRecord

import scala.collection.mutable


case class FileReadData(string: String,
                        baseFile: FileHandle,
                        physicalFile: String,
                        readEndPos: Long,
                        writeTimestamp: Long,
                        readTimestamp: Long,
                        newerFilesWithSharedLastModified: Int)

class SendBuffer(fileReaderManager: FileReaderManager, readPersistence: ReadPersistence) {

  private val buffer: mutable.Queue[FileReadData] = mutable.Queue.empty

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

  private def ensureFilledIfPossible(): Unit = {
    
    if (buffer.size <= 1) {
      fileReaderManager.getNextString(fileReadData => {
        buffer.enqueue(fileReadData)
        readPersistence.completeRead(fileReadData.baseFile, FileReadRecord(fileReadData.readEndPos, fileReadData.writeTimestamp, newerFilesWithSharedLastModified=fileReadData.newerFilesWithSharedLastModified))
      })
    }
  }
}

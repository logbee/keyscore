package io.logbee.keyscore.pipeline.contrib.tailin

import scala.collection.mutable.Queue
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReaderManager

class SendBuffer(fileReaderManager: FileReaderManager) {
  private var buffer: Queue[String] = Queue.empty
  
  def addToBuffer(string: String) = {
    buffer.enqueue(string)
  }
  
  def getNextElement: String = {
    
    ensureFilledIfPossible()
    
    buffer.dequeue()
  }
  
  def isEmpty: Boolean = {
    
    ensureFilledIfPossible()
    
    buffer.isEmpty
  }
  
  
  private def ensureFilledIfPossible() = {
    if (buffer.size <= 1) { //TODO make this asynchronous, with enough buffer size to not likely run into delays
      fileReaderManager.getNextString(string => buffer.enqueue(string))
    }
  }
}
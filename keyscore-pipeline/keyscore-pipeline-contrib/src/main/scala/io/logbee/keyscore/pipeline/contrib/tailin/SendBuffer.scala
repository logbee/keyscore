package io.logbee.keyscore.pipeline.contrib.tailin

import scala.collection.mutable.Queue

class SendBuffer {
  private var buffer: Queue[String] = Queue.empty
  
  def addToBuffer(string: String) = {
    buffer.enqueue(string)
  }
  
  def getNextElement: String = {
    buffer.dequeue()
  }
  
  def isEmpty: Boolean = {
    buffer.isEmpty
  }
}
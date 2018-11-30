package io.logbee.keyscore.pipeline.contrib.tailin.send

class SendBuffer {
  private var buffer: Seq[String] = Seq.empty
  
  def addToBuffer(string: String) = {
    buffer = buffer :+ string
  }
  
  def getNextElement: String = {
    
    val returnVal = buffer.head
    
    buffer = buffer.drop(1)
    
    returnVal
  }
  
  def isEmpty: Boolean = {
    buffer.isEmpty
  }
}
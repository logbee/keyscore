package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.nio.{Buffer, CharBuffer}

import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader.CharPos

object CharBufferUtil {
  
  def getBufferSectionAsString(buffer: CharBuffer, startPos: CharPos, endPos: CharPos): String = {
    
    val length = endPos - startPos
    
    val tmpPos = buffer.position()
    buffer.asInstanceOf[Buffer].position(startPos.value)
    
    val array = new Array[Char](length.value)
    buffer.get(array, 0, length.value)
    val returnString = new String(array)
    
    buffer.asInstanceOf[Buffer].position(tmpPos) //set the position back
    
    returnString
  }

  /**
   * Given a buffer and a position in this buffer that contains a newline-char, this method will return the next position that does not contain a newline-char.
   * 
   * @return The next position to not contain a newline-char.
   */
  def getStartOfNextLine(buffer: CharBuffer, position: CharPos): CharPos = {
    
    if (buffer.get(position.value) != '\n' && buffer.get(position.value) != '\r') {
      throw new IllegalArgumentException("The given starting position does not contain a newline-char.")
    }
    
    var _position = position.value
    
    while (_position < buffer.limit() &&
        (buffer.get(_position) == '\n' || buffer.get(_position) == '\r')) { //skip any following newline-chars, which explicitly includes "\n", "\r", "\r\n", as well as "\n\n\n\n\n\n\n..."
      _position += 1
    }
    
    CharPos(_position)
  }
  
}

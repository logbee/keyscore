package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.nio.CharBuffer

object CharBufferUtil {
  
  def getBufferSectionAsString(buffer: CharBuffer, position: Int, length: Int): String = {
    
    val tmpPos = buffer.position()
    buffer.position(position)
    
    var array = new Array[Char](length)
    buffer.get(array, 0, length)
    val returnString = new String(array)
    
    buffer.position(tmpPos) //set the position back
    
    returnString
  }

  /**
   * Given a buffer and a position in this buffer that contains a newline-char, this method will return the next position that does not contain a newline-char.
   * 
   * @return The next position to not contain a newline-char.
   */
  def getStartOfNextLine(buffer: CharBuffer, position: Int): Int = {
    
    if (buffer.get(position) != '\n' && buffer.get(position) != '\r') {
      throw new IllegalArgumentException("The given starting position does not contain a newline-char.")
    }
    
    var _position = position
    
    while (_position < buffer.limit() &&
        (buffer.get(_position) == '\n' || buffer.get(_position) == '\r')) { //skip any following newline-chars, which explicitly includes "\n", "\r", "\r\n", as well as "\n\n\n\n\n\n\n..."
      _position += 1
    }
    
    _position
  }
  
}

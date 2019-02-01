package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.io.File
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.FileChannel
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardOpenOption

import org.slf4j.LoggerFactory

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.read.ReadMode.ReadMode
import io.logbee.keyscore.pipeline.contrib.tailin.util.CharBufferUtil


object ReadMode extends Enumeration {
  type ReadMode = Value
  val LINE, FILE = Value
}

case class FileReadRecord(previousReadPosition: Long, previousReadTimestamp: Long)



/**
 * @param rotationPattern Glob-pattern for the file-name of rotated files. If an empty string or null is passed, no rotated files are matched.
 */
class FileReader(fileToRead: File, rotationPattern: String, byteBufferSize: Int, charset: Charset, readMode: ReadMode) {
  
  private val log = LoggerFactory.getLogger(classOf[FileReader])
  
  log.info("Instantiated for " + fileToRead)
  var leftOverFromPreviousBuffer = ""
  
  
  //TODO move the fileReadChannel up to here, tear it down in tearDown()
  
  def read(callback: FileReadData => Unit, readScheduleItem: ReadScheduleItem) = {
    println("readScheduleItem.endPos: " + readScheduleItem.endPos)
    val decoder = charset.newDecoder
    decoder.onMalformedInput(CodingErrorAction.REPORT)
    
    val charBufferSize = Math.ceil(byteBufferSize * charset.newDecoder.maxCharsPerByte).asInstanceOf[Int] //enough space to decode a full byteBuffer
    val charBuffer = CharBuffer.allocate(charBufferSize)
    
    val byteBuffer = ByteBuffer.allocate(byteBufferSize)
    
    assert(readScheduleItem.endPos <= fileToRead.length) //TODO
    
    var nextBufferStartPosition = readScheduleItem.startPos
    
    if (nextBufferStartPosition < readScheduleItem.endPos) { //skip creating a fileReadChannel and persisting the data, if there is nothing to read
      var fileReadChannel: FileChannel = null
      try {
        fileReadChannel = Files.newByteChannel(fileToRead.toPath, StandardOpenOption.READ).asInstanceOf[FileChannel]
        
        
        while (nextBufferStartPosition < readScheduleItem.endPos) {
          
          //we're reading and persisting byte positions, because the variable length of encoded chars would mean that we can't resume reading at the same position without decoding every single char in the whole char sequence (file) before it
          
          byteBuffer.clear()
          var bytesRead = fileReadChannel.read(byteBuffer, nextBufferStartPosition)
          
          if (bytesRead == -1 || bytesRead == 0) {
            throw new IllegalStateException("There were no bytes to read.")
          }
          
          byteBuffer.flip() //sets limit to final read position, so that buffer.position can be used as pointer
          
          
          charBuffer.clear()
          decoder.reset()
          val coderResult = decoder.decode(byteBuffer, charBuffer, true)
          
          if (coderResult.isMalformed) {
            
            if (byteBuffer.position + coderResult.length == byteBuffer.capacity) { //characters are malformed because of the end of the buffer
              bytesRead -= coderResult.length
            }
            else { //actual error case
              throw new CharacterCodingException
            }
          }
          
          
          charBuffer.flip()
          
          processBufferContents(charBuffer, callback, nextBufferStartPosition, (readScheduleItem.endPos - charBuffer.limit).asInstanceOf[Int], readScheduleItem.writeTimestamp)
          
          
          nextBufferStartPosition += bytesRead
        }
      }
      finally {
        if (fileReadChannel != null) {
          fileReadChannel.close()
        }
      }
    }
  }
  
  
  
  private def processBufferContents(charBuffer: CharBuffer, callback: FileReadData => Unit, bufferStartPositionInFile: Long, readEndPositionInBuffer: Int, callbackWriteTimestamp: Long) = {
    println("readEndPositionInBuffer: " + readEndPositionInBuffer)
    println("charBuffer: " + charBuffer)
    val limit = Math.min(charBuffer.limit, readEndPositionInBuffer) //FIXME readEndPositionInBuffer muss in chars umgewandelt werden
    
    
    var byteCompletedPositionWithinBuffer = 0
    
    if (readMode == ReadMode.FILE) {
      //do nothing
      charBuffer.position(0)
    }
    else if (readMode == ReadMode.LINE) {
      
      var charCompletedPositionWithinBuffer = 0
      
      while (charBuffer.position < limit) {
        
        //check for the occurrence of \n or \r, as we do linewise reading
        val char = charBuffer.get().toChar //sets pos to pos+1
        
        if (char == '\n' || char == '\r') {
          
          charBuffer.position(charBuffer.position - 1)
          
          val firstNewlineCharPosWithinBuffer = charBuffer.position
          
          val charPosEndOfNewlines = CharBufferUtil.getStartOfNextLine(charBuffer, charBuffer.position)
          val stringWithNewlines = CharBufferUtil.getBufferSectionAsString(charBuffer, charCompletedPositionWithinBuffer, charPosEndOfNewlines - charCompletedPositionWithinBuffer)
          val string = stringWithNewlines.substring(0, firstNewlineCharPosWithinBuffer - charCompletedPositionWithinBuffer)
          
          charCompletedPositionWithinBuffer += stringWithNewlines.length
          byteCompletedPositionWithinBuffer += stringWithNewlines.getBytes.length
          
          println("kasjdalksjdlaksjdlkasjdlaksjdalskjd")
          doCallback(callback, string, bufferStartPositionInFile + byteCompletedPositionWithinBuffer, callbackWriteTimestamp)
          
          charBuffer.position(charPosEndOfNewlines)
        }
      }
      
      //if end of buffer reached without finding another newline
      charBuffer.position(charCompletedPositionWithinBuffer) //reset to the previous written position, so that the rest of the buffer can be read out
    }
    
    
    //read out rest of buffer
    val remainingNumberOfCharsInBuffer = limit - charBuffer.position
    
    if (remainingNumberOfCharsInBuffer > 0) {
      println("rest of buffer")
      println("limit: " + limit)
      println("charBuffer.position: " + charBuffer.position)
      println("byteCompletedPositionWithinBuffer: " + byteCompletedPositionWithinBuffer)
      println("remainingNumberOfCharsInBuffer: " + remainingNumberOfCharsInBuffer)
      val string = CharBufferUtil.getBufferSectionAsString(charBuffer, charBuffer.position, remainingNumberOfCharsInBuffer)
      byteCompletedPositionWithinBuffer += string.getBytes.length
      
      if (charBuffer.limit < charBuffer.capacity) { //end of file
        doCallback(callback, string, bufferStartPositionInFile + byteCompletedPositionWithinBuffer, callbackWriteTimestamp) //write the remaining bytes
      }
      else { //not end of file
        leftOverFromPreviousBuffer += string //store the remaining bytes, to be written later
      }
    }
  }
  
  
  private def doCallback(callback: FileReadData => Unit, string: String, readEndPos: Long, writeTimestamp: Long) = {
    println("doCallback: " + readEndPos)
    val fileReadData = FileReadData(leftOverFromPreviousBuffer + string, fileToRead, readEndPos, writeTimestamp)
    
    callback(fileReadData)
    leftOverFromPreviousBuffer = ""
  }
  
  
  def pathDeleted() {
    tearDown()
  }
  
  def tearDown() = {
    log.info("Teardown for " + fileToRead)
  }
}

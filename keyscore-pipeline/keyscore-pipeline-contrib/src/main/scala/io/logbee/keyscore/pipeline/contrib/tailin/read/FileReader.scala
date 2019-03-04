package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.io.File
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.FileChannel
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
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


object FileReader {
  
  /**
   * These are basically wrappers around Longs/Ints to prevent accidentally adding char positions onto byte positions (or vice versa).
   * UTF-8, for example can have 1 to 4 bytes per char.
   */
  case class BytePos(var value: Long) {
    def <(other: BytePos): Boolean = {
      this.value < other.value
    }
    
    def +(other: BytePos): BytePos = {
      BytePos(this.value + other.value)
    }
    
    def -(other: BytePos): BytePos = {
      BytePos(this.value - other.value)
    }
  }
  
  
  case class CharPos(var value: Int) {
    def <(other: CharPos): Boolean = {
      this.value < other.value
    }
    
    def +(other: CharPos): CharPos = {
      CharPos(this.value + other.value)
    }
    
    def -(other: CharPos): CharPos = {
      CharPos(this.value - other.value)
    }
  }
}


/**
 * @param rotationPattern Glob-pattern for the file-name of rotated files. If an empty string or null is passed, no rotated files are matched.
 */
class FileReader(fileToRead: File, rotationPattern: String, byteBufferSize: Int, charset: Charset, readMode: ReadMode) {
  
  import FileReader.BytePos
  import FileReader.CharPos
  
  
  private val log = LoggerFactory.getLogger(classOf[FileReader])
  
  private val decoder = charset.newDecoder
  decoder.onMalformedInput(CodingErrorAction.REPORT)
  
  private val charBufferSize = Math.ceil(byteBufferSize * charset.newDecoder.maxCharsPerByte).asInstanceOf[Int] //enough space to decode a full byteBuffer
  private val charBuffer = CharBuffer.allocate(charBufferSize)
  
  private val byteBuffer = ByteBuffer.allocate(byteBufferSize)
  
  private val fileReadChannel = Files.newByteChannel(fileToRead.toPath, StandardOpenOption.READ).asInstanceOf[FileChannel]
  
  
  private var leftOverFromPreviousBuffer = ""
  
  
  
  def read(callback: FileReadData => Unit, readScheduleItem: ReadScheduleItem) = {
    
    val readEndPos = BytePos(readScheduleItem.endPos)
    
    
    
    assert(readScheduleItem.startPos <= readEndPos.value) //TODO
    assert(readEndPos.value <= fileToRead.length) //TODO
    
    var bufferStartPos = BytePos(readScheduleItem.startPos)
    
    while (bufferStartPos < readEndPos) {
      
      //we're reading and persisting byte positions, because the variable byte-length of encoded chars means
      //that we can't resume reading at the same position without decoding every single char in the whole char sequence (file) before it
      
      byteBuffer.clear()
      val newBufferLimit = (readEndPos - bufferStartPos).value.asInstanceOf[Int]
      if (newBufferLimit < byteBufferSize)
        byteBuffer.limit(newBufferLimit) //set the limit to the end of what it should read out
        
      var bytesRead = BytePos(fileReadChannel.read(byteBuffer, bufferStartPos.value))
      
      if (bytesRead.value == -1 || bytesRead.value == 0) {
        throw new IllegalStateException("There were no bytes to read.")
      }
      
      byteBuffer.flip() //sets limit to final read position, so that buffer.position can be used as pointer
      
      
      charBuffer.clear()
      decoder.reset()
      val coderResult = decoder.decode(byteBuffer, charBuffer, true)
      
      if (coderResult.isMalformed) {
        
        if (byteBuffer.position + coderResult.length == byteBuffer.capacity) { //characters are malformed because of the end of the buffer
          bytesRead -= BytePos(coderResult.length)
        }
        else { //actual error case
          throw new CharacterCodingException
        }
      }
      
      
      charBuffer.flip()
      
      processBufferContents(charBuffer, callback, bufferStartPos, readEndPos, readScheduleItem.writeTimestamp)
      
      
      bufferStartPos += bytesRead
    }
  }
  
  
  
  private def processBufferContents(charBuffer: CharBuffer, callback: FileReadData => Unit, bufferStartPositionInFile: BytePos, readEndPosition: BytePos, callbackWriteTimestamp: Long) = {
    
    var byteCompletedPositionWithinBuffer = BytePos(0)
    
    
    readMode match {
      case ReadMode.FILE => {
        //do nothing here (read out rest of buffer below)
        charBuffer.position(0)
      }
      case ReadMode.LINE => {
        
        var charCompletedPositionWithinBuffer = CharPos(0)
        
        while (charBuffer.position < charBuffer.limit) {
          //check for the occurrence of \n or \r, as we do linewise reading
          val char = charBuffer.get().toChar //sets pos to pos+1
          
          if (char == '\n' || char == '\r') {
            
            charBuffer.position(charBuffer.position - 1)
            
            val firstNewlineCharPosWithinBuffer = CharPos(charBuffer.position)
            
            val charPosEndOfNewlines = CharBufferUtil.getStartOfNextLine(charBuffer, firstNewlineCharPosWithinBuffer)
            val stringWithNewlines = CharBufferUtil.getBufferSectionAsString(charBuffer, charCompletedPositionWithinBuffer, charPosEndOfNewlines - charCompletedPositionWithinBuffer)
            val string = stringWithNewlines.substring(0, (firstNewlineCharPosWithinBuffer - charCompletedPositionWithinBuffer).value)
            
            charCompletedPositionWithinBuffer += CharPos(stringWithNewlines.length)
            byteCompletedPositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit)
            
            
            doCallback(callback, string, bufferStartPositionInFile + byteCompletedPositionWithinBuffer, callbackWriteTimestamp)
            
            charBuffer.position(charPosEndOfNewlines.value)
          }
        }
        
        //if end of buffer reached without finding another newline
        charBuffer.position(charCompletedPositionWithinBuffer.value) //reset to the previous written position, so that the rest of the buffer can be read out
      }
    }
    
    
    //read out rest of buffer
    val remainingNumberOfCharsInBuffer = CharPos(charBuffer.limit - charBuffer.position)
    
    if (remainingNumberOfCharsInBuffer.value > 0) {
      val string = CharBufferUtil.getBufferSectionAsString(charBuffer, CharPos(charBuffer.position), remainingNumberOfCharsInBuffer)
      byteCompletedPositionWithinBuffer += BytePos(charset.encode(string).limit)
      
      
      if (bufferStartPositionInFile + byteCompletedPositionWithinBuffer == readEndPosition) { //completed reading
        doCallback(callback, string, readEndPosition, callbackWriteTimestamp)
      }
      else { //not yet completed reading, i.e. another buffer is going to get filled and will continue where this one ended
        leftOverFromPreviousBuffer += string //store the remaining bytes, to be written later
      }
    }
  }
  
  
  private def doCallback(callback: FileReadData => Unit, string: String, readEndPos: BytePos, writeTimestamp: Long) = {
    
    val fileReadData = FileReadData(leftOverFromPreviousBuffer + string, fileToRead, readEndPos.value, writeTimestamp)
    
    callback(fileReadData)
    leftOverFromPreviousBuffer = ""
  }
  
  
  def pathDeleted() {
    tearDown()
  }
  
  
  def tearDown() = {
    if (fileReadChannel != null) {
      fileReadChannel.close()
    }
    
    log.info("Teardown for " + fileToRead)
  }
}

package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.read.ReadMode.ReadMode
import io.logbee.keyscore.pipeline.contrib.tailin.util.CharBufferUtil
import org.slf4j.LoggerFactory


object ReadMode extends Enumeration {
  type ReadMode = Value
  val LINE, FILE = Value
}

case class FileReadRecord(previousReadPosition: Long, previousReadTimestamp: Long, newerFilesWithSharedLastModified: Int)


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
class FileReader(fileToRead: FileHandle, rotationPattern: String, byteBufferSize: Int, charset: Charset, readMode: ReadMode, fileCompleteActions: Seq[FileHandle => Unit]) {
  
  import FileReader.BytePos
  import FileReader.CharPos
  
  private lazy val log = LoggerFactory.getLogger(classOf[FileReader])
  
  
  private val decoder = charset.newDecoder
  decoder.onMalformedInput(CodingErrorAction.REPORT)
  
  private val charBufferSize = Math.ceil(byteBufferSize * charset.newDecoder.maxCharsPerByte).asInstanceOf[Int] //enough space to decode a full byteBuffer
  private val charBuffer = CharBuffer.allocate(charBufferSize)
  
  private val byteBuffer = ByteBuffer.allocate(byteBufferSize)
  
  
  private var leftOverFromPreviousBuffer = ""
  
  
  
  def read(callback: FileReadData => Unit, readScheduleItem: ReadScheduleItem): Unit = {
    
    log.debug("Reading from file '{}'.", fileToRead)
    
    val readEndPos = BytePos(readScheduleItem.endPos)
    
    
    var bufferStartPos = BytePos(readScheduleItem.startPos)
    
    while (bufferStartPos < readEndPos) {
      
      //we're reading and persisting byte-positions, because the variable byte-length of encoded chars means that we
      //can't resume at the saved char-position without decoding every single char before it (to find out its byte-length)
      
      byteBuffer.clear()
      val newBufferLimit = (readEndPos - bufferStartPos).value.asInstanceOf[Int]
      if (newBufferLimit < byteBufferSize)
        byteBuffer.limit(newBufferLimit) //set the limit to the end of what it should read out
        
      var bytesRead = BytePos(fileToRead.read(byteBuffer, bufferStartPos.value))
      
      if (bytesRead.value < 1) {
        throw new IllegalStateException("There were no bytes to read.")
      }
      
      
      byteBuffer.rewind()
      
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
      
      processBufferContents(charBuffer, callback, bufferStartPos, readEndPos, readScheduleItem.lastModified, readScheduleItem.newerFilesWithSharedLastModified)
      
      
      bufferStartPos += bytesRead
    }
  }
  
  
  
  private def processBufferContents(charBuffer: CharBuffer, callback: FileReadData => Unit, bufferStartPositionInFile: BytePos, readEndPosition: BytePos, callbackWriteTimestamp: Long, newerFilesWithSharedLastModified: Int): Unit = {
    var completedBytePositionWithinBuffer = BytePos(0)
    
    
    readMode match {
      case ReadMode.FILE => {
        //do nothing here (read out rest of buffer below)
        charBuffer.position(0)
      }
      case ReadMode.LINE => {
        
        var completedCharPositionWithinBuffer = CharPos(0)
        
        while (charBuffer.position < charBuffer.limit) {
          //check for the occurrence of \n or \r, as we do linewise reading
          val char = charBuffer.get() //sets pos to pos+1
          
          if (char == '\n' || char == '\r') {
            
            charBuffer.position(charBuffer.position - 1)
            
            val firstNewlineWithinBuffer = CharPos(charBuffer.position)
            
            val endOfNewlines: CharPos = CharBufferUtil.getStartOfNextLine(charBuffer, firstNewlineWithinBuffer)
            val stringWithNewlines = CharBufferUtil.getBufferSectionAsString(charBuffer, completedCharPositionWithinBuffer, endOfNewlines - completedCharPositionWithinBuffer)
            val string = stringWithNewlines.substring(0, (firstNewlineWithinBuffer - completedCharPositionWithinBuffer).value)
            
            completedCharPositionWithinBuffer += CharPos(stringWithNewlines.length)
            completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit)
            
            
            doCallback(callback, string, bufferStartPositionInFile + completedBytePositionWithinBuffer, callbackWriteTimestamp, newerFilesWithSharedLastModified)
            
            charBuffer.position(endOfNewlines.value)
          }
        }
        
        //if end of buffer reached without finding another newline
        charBuffer.position(completedCharPositionWithinBuffer.value) //reset to the previous written position, so that the rest of the buffer can be read out
      }
    }
    
    //read out rest of buffer
    val remainingNumberOfCharsInBuffer = CharPos(charBuffer.limit - charBuffer.position)
    
    if (remainingNumberOfCharsInBuffer.value > 0) {
      val string = CharBufferUtil.getBufferSectionAsString(charBuffer, CharPos(charBuffer.position), remainingNumberOfCharsInBuffer)
      completedBytePositionWithinBuffer += BytePos(charset.encode(string).limit)
      
      if (bufferStartPositionInFile + completedBytePositionWithinBuffer == readEndPosition) { //completed reading
        doCallback(callback, string, readEndPosition, callbackWriteTimestamp, newerFilesWithSharedLastModified)
      }
      else { //not yet completed reading, i.e. another buffer is going to get filled and will continue where this one ended
        leftOverFromPreviousBuffer += string //store the remaining bytes, to be written later
      }
    }
    
    
    if (bufferStartPositionInFile + completedBytePositionWithinBuffer == BytePos(fileToRead.length))
      fileCompleteActions.foreach(action => action(fileToRead))
  }
  
  
  private def doCallback(callback: FileReadData => Unit, string: String, readEndPos: BytePos, writeTimestamp: Long, newerFilesWithSharedLastModified: Int): Unit = {
    
    val fileReadData = FileReadData(string=leftOverFromPreviousBuffer + string,
                                    baseFile=null,
                                    physicalFile=fileToRead.absolutePath,
                                    readEndPos=readEndPos.value,
                                    writeTimestamp=writeTimestamp,
                                    readTimestamp=System.currentTimeMillis,
                                    newerFilesWithSharedLastModified=newerFilesWithSharedLastModified)
    
    callback(fileReadData)
    leftOverFromPreviousBuffer = ""
  }
  
  
  def pathDeleted(): Unit = tearDown()
  
  
  def tearDown(): Unit = fileToRead.tearDown()
}

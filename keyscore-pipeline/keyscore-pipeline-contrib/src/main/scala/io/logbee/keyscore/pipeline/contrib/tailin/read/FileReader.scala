package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.{CharacterCodingException, Charset, CodingErrorAction}
import java.nio.{Buffer, ByteBuffer, CharBuffer}

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.read.PostReadFileAction.PostReadFileActionFunc
import io.logbee.keyscore.pipeline.contrib.tailin.util.CharBufferUtil
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

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

class FileReader(fileToRead: FileHandle, byteBufferSize: Int, charset: Charset, readMode: ReadMode, postReadFileActionFunc: PostReadFileActionFunc) {
  
  import FileReader.{BytePos, CharPos}
  
  private lazy val log = LoggerFactory.getLogger(classOf[FileReader])

  private val decoder = charset.newDecoder
  decoder.onMalformedInput(CodingErrorAction.REPORT)
  
  private val charBufferSize = Math.ceil(byteBufferSize * charset.newDecoder.maxCharsPerByte).asInstanceOf[Int] //enough space to decode a full byteBuffer
  private val charBuffer = CharBuffer.allocate(charBufferSize)
  
  private val byteBuffer = ByteBuffer.allocate(byteBufferSize)


  private var readDataToCallback = ""
  
  
  /**
    * In MultiLine-mode, whether data should be added to be called back (pushed).
    * 
    * This is not the case when the firstLinePattern has not been found yet
    * or after the lastLinePattern in MultiLineWithEnd-mode (before the next firstLinePattern is found).
    */
  private var multiLineAddToCallbackEnabled = false
  

  def read(callback: FileReadData => Unit, readScheduleItem: ReadScheduleItem): Unit = {

    val readEndPos = BytePos(readScheduleItem.endPos)
    var bufferStartPos = BytePos(readScheduleItem.startPos)

    fileToRead.open {

      case Success(file) =>
        log.debug("Reading from {}.", fileToRead)

        while (bufferStartPos < readEndPos) {

          //we're reading and persisting byte-positions, because the variable byte-length of encoded chars means that we
          //can't resume at the saved char-position without decoding every single char before it (to find out its byte-length)

          byteBuffer.asInstanceOf[Buffer].clear() //casting to Buffer fixes compiling issues with JDK9
          val newBufferLimit = (readEndPos - bufferStartPos).value.asInstanceOf[Int]
          if (newBufferLimit < byteBufferSize)
            byteBuffer.asInstanceOf[Buffer].limit(newBufferLimit) //set the limit to the end of what it should read out

          var bytesRead = BytePos(file.read(byteBuffer, bufferStartPos.value))

          byteBuffer.asInstanceOf[Buffer].rewind()

          charBuffer.asInstanceOf[Buffer].clear()
          decoder.reset()
          val coderResult = decoder.decode(byteBuffer, charBuffer, true)

          if (coderResult.isMalformed) {

            if (byteBuffer.position() + coderResult.length == byteBuffer.capacity) { //characters are malformed because of the end of the buffer
              bytesRead -= BytePos(coderResult.length)
            }
            else { //actual error case
              throw new CharacterCodingException
            }
          }

          charBuffer.asInstanceOf[Buffer].flip()

          processBufferContents(
            charBuffer,
            callback,
            bufferStartPos,
            file.length,
            file.absolutePath,
            readScheduleItem,
          )

          bufferStartPos += bytesRead
        }

      case Failure(ex) =>
        log.error("Could not read from file '{}': {}", fileToRead, ex)
      }
  }

  private def processBufferContents(
    charBuffer: CharBuffer,
    callback: FileReadData => Unit,
    bufferStartPositionInFile: BytePos,
    fileLength: Long,
    absolutePath: String,
    readScheduleItem: ReadScheduleItem,
  ): Unit = {

    var completedBytePositionWithinBuffer = BytePos(0)

    readMode match {
      case ReadMode.Line | ReadMode.MultiLine(_) | ReadMode.MultiLineWithEnd(_, _) => {

        var completedCharPositionWithinBuffer = CharPos(0)

        while (charBuffer.position() < charBuffer.limit()) {
          //check for the occurrence of \n or \r, as we do linewise reading
          val char = charBuffer.get() //sets pos to pos+1

          if (char == '\n' || char == '\r') {

            charBuffer.asInstanceOf[Buffer].position(charBuffer.position() - 1)

            val firstNewline = CharPos(charBuffer.position())

            val endOfNewlines: CharPos = CharBufferUtil.getStartOfNextLine(charBuffer, firstNewline)
            val stringWithNewlines = CharBufferUtil.getBufferSectionAsString(charBuffer, completedCharPositionWithinBuffer, endOfNewlines)
            val string = stringWithNewlines.substring(0, (firstNewline - completedCharPositionWithinBuffer).value)

            completedCharPositionWithinBuffer += CharPos(stringWithNewlines.length)
            
            
            
            readMode match {
              case ReadMode.Line => {
                readDataToCallback += string
                completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit())
                doCallback(
                  callback,
                  bufferStartPositionInFile + completedBytePositionWithinBuffer,
                  readScheduleItem,
                  absolutePath,
                )
              }
              case ReadMode.MultiLine(firstLineRegex) if multiLineAddToCallbackEnabled == false => { //disabled when starting to read a file, gets enabled when the first line matching firstLinePattern is found
                string match {
                  case firstLineRegex() =>
                    multiLineAddToCallbackEnabled = true
                    readDataToCallback += stringWithNewlines
                }
                
                completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit())
              }
              case ReadMode.MultiLine(firstLineRegex) if multiLineAddToCallbackEnabled == true => {
                string match {
                  case firstLineRegex() =>
                    doCallback( //callback the previously read lines
                    callback,
                    bufferStartPositionInFile + completedBytePositionWithinBuffer,
                    readScheduleItem,
                    absolutePath,
                  )
                  case _ => //do nothing
                }
                
                readDataToCallback += stringWithNewlines
                completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit())
              }
              case ReadMode.MultiLineWithEnd(firstLineRegex, _) if multiLineAddToCallbackEnabled == false => { //disabled at the beginning of the file, and between finding a line matching lastLinePattern and finding a line matching firstLinePattern
                completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit())
                string match {
                  case firstLineRegex() =>
                    doCallback( //callback the previously read lines
                      callback,
                      bufferStartPositionInFile + completedBytePositionWithinBuffer,
                      readScheduleItem,
                      absolutePath
                    )
                    multiLineAddToCallbackEnabled = true
                    readDataToCallback += stringWithNewlines
                  case _ => //do nothing
                }
              }
              case ReadMode.MultiLineWithEnd(firstLineRegex, lastLineRegex) if multiLineAddToCallbackEnabled == true => {
                string match {
                  case firstLineRegex() => {
                    doCallback( //callback the previously read lines
                      callback,
                      bufferStartPositionInFile + completedBytePositionWithinBuffer,
                      readScheduleItem,
                      absolutePath
                    )
                    
                    readDataToCallback += stringWithNewlines
                    completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit())
                  }
                  case lastLineRegex() => {
                    readDataToCallback += string
                    completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit())
                    
                    multiLineAddToCallbackEnabled = false
                  }
                  case _ => {
                    if (multiLineAddToCallbackEnabled) {
                      readDataToCallback += stringWithNewlines
                    }
                    
                    completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit())
                  }
                }
              }
              case ReadMode.File => ??? //match-case above should never allow this to be triggered
            }
            
            charBuffer.asInstanceOf[Buffer].position(endOfNewlines.value)
          }
        }

        //if end of buffer reached without finding another newline
        charBuffer.asInstanceOf[Buffer].position(completedCharPositionWithinBuffer.value) //reset to the previous written position, so that the rest of the buffer can be read out
      }
      case ReadMode.File => {
        //do nothing here (read out rest of buffer below)
        charBuffer.asInstanceOf[Buffer].position(0)
      }
    }

    //read out rest of buffer
    if (charBuffer.limit() - charBuffer.position() > 0) {
      val string = CharBufferUtil.getBufferSectionAsString(charBuffer, CharPos(charBuffer.position()), CharPos(charBuffer.limit()))
      completedBytePositionWithinBuffer += BytePos(charset.encode(string).limit())
      
      readDataToCallback += string
    }
    
    val completedPositionInFile = bufferStartPositionInFile + completedBytePositionWithinBuffer
    if (completedPositionInFile == BytePos(readScheduleItem.endPos)) { //completed reading
      doCallback(
        callback,
        completedPositionInFile,
        readScheduleItem,
        absolutePath,
      )
    }
    

    if (completedPositionInFile == BytePos(fileLength))
      postReadFileActionFunc(fileToRead)
  }

  private def doCallback(callback: FileReadData => Unit, readEndPos: BytePos, readScheduleItem: ReadScheduleItem, absolutePath: String): Unit = {
    if (readDataToCallback.isEmpty == false) {
      val fileReadData = FileReadData(readData=readDataToCallback,
                                      baseFile=null,
                                      physicalFile=absolutePath,
                                      readEndPos=readEndPos.value,
                                      writeTimestamp=readScheduleItem.lastModified,
                                      readTimestamp=System.currentTimeMillis,
                                      newerFilesWithSharedLastModified=readScheduleItem.newerFilesWithSharedLastModified)
      
      callback(fileReadData)
      readDataToCallback = ""
    }
  }
}

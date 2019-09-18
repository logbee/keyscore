package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.{CharacterCodingException, Charset, CodingErrorAction}

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
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

  case class FileReadRecord(previousReadPosition: Long, previousReadTimestamp: Long, newerFilesWithSharedLastModified: Int)
}

/**
 * @param rotationPattern Glob-pattern for the file-name of rotated files. If an empty string or null is passed, no rotated files are matched.
 */
class FileReader(fileToRead: FileHandle, rotationPattern: String, byteBufferSize: Int, charset: Charset, readMode: ReadMode, firstLinePattern: String = ".*", fileCompleteActions: Seq[FileHandle => Unit]) {
  
  import FileReader.{BytePos, CharPos}
  
  private lazy val log = LoggerFactory.getLogger(classOf[FileReader])

  private val decoder = charset.newDecoder
  decoder.onMalformedInput(CodingErrorAction.REPORT)
  
  private val charBufferSize = Math.ceil(byteBufferSize * charset.newDecoder.maxCharsPerByte).asInstanceOf[Int] //enough space to decode a full byteBuffer
  private val charBuffer = CharBuffer.allocate(charBufferSize)
  
  private val byteBuffer = ByteBuffer.allocate(byteBufferSize)

  private var readDataToCallback = ""

  def read(callback: FileReadData => Unit, readScheduleItem: ReadScheduleItem): Unit = {

    log.debug("Reading from {}.", fileToRead)

    val readEndPos = BytePos(readScheduleItem.endPos)
    var bufferStartPos = BytePos(readScheduleItem.startPos)

    fileToRead.open {

      case Success(file) =>

        while (bufferStartPos < readEndPos) {

          //we're reading and persisting byte-positions, because the variable byte-length of encoded chars means that we
          //can't resume at the saved char-position without decoding every single char before it (to find out its byte-length)

          byteBuffer.clear()
          val newBufferLimit = (readEndPos - bufferStartPos).value.asInstanceOf[Int]
          if (newBufferLimit < byteBufferSize)
            byteBuffer.limit(newBufferLimit) //set the limit to the end of what it should read out

          var bytesRead = BytePos(file.read(byteBuffer, bufferStartPos.value))

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
      case ReadMode.Line | ReadMode.MultiLine => {

        var completedCharPositionWithinBuffer = CharPos(0)

        while (charBuffer.position < charBuffer.limit) {
          //check for the occurrence of \n or \r, as we do linewise reading
          val char = charBuffer.get() //sets pos to pos+1

          if (char == '\n' || char == '\r') {

            charBuffer.position(charBuffer.position - 1)

            val firstNewline = CharPos(charBuffer.position)

            val endOfNewlines: CharPos = CharBufferUtil.getStartOfNextLine(charBuffer, firstNewline)
            val stringWithNewlines = CharBufferUtil.getBufferSectionAsString(charBuffer, completedCharPositionWithinBuffer, endOfNewlines)
            val string = stringWithNewlines.substring(0, (firstNewline - completedCharPositionWithinBuffer).value)

            completedCharPositionWithinBuffer += CharPos(stringWithNewlines.length)
            


            readMode match {
              case ReadMode.Line => {
                readDataToCallback += string
                completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit)
                doCallback(
                  callback,
                  bufferStartPositionInFile + completedBytePositionWithinBuffer,
                  readScheduleItem,
                  absolutePath,
                )
              }
              case ReadMode.MultiLine => {
                if (string.matches(firstLinePattern)) {
                  doCallback( //callback the previously read lines
                    callback,
                    bufferStartPositionInFile + completedBytePositionWithinBuffer,
                    readScheduleItem,
                    absolutePath,
                  )
                }
                readDataToCallback += stringWithNewlines
                completedBytePositionWithinBuffer += BytePos(charset.encode(stringWithNewlines).limit)
              }
            }
            
            charBuffer.position(endOfNewlines.value)
          }
        }

        //if end of buffer reached without finding another newline
        charBuffer.position(completedCharPositionWithinBuffer.value) //reset to the previous written position, so that the rest of the buffer can be read out
      }
      case ReadMode.File => {
        //do nothing here (read out rest of buffer below)
        charBuffer.position(0)
      }
    }

    //read out rest of buffer
    if (charBuffer.limit - charBuffer.position > 0) {
      val string = CharBufferUtil.getBufferSectionAsString(charBuffer, CharPos(charBuffer.position), CharPos(charBuffer.limit))
      completedBytePositionWithinBuffer += BytePos(charset.encode(string).limit)
      
      readDataToCallback += string
    }
    
    if (bufferStartPositionInFile + completedBytePositionWithinBuffer == BytePos(readScheduleItem.endPos)) { //completed reading
        doCallback(
          callback,
          BytePos(readScheduleItem.endPos),
          readScheduleItem,
          absolutePath,
        )
      }
    

    if (bufferStartPositionInFile + completedBytePositionWithinBuffer == BytePos(fileLength))
      fileCompleteActions.foreach(action => action(fileToRead))
  }

  private def doCallback(callback: FileReadData => Unit, readEndPos: BytePos, readScheduleItem: ReadScheduleItem, absolutePath: String): Unit = {
    if (readDataToCallback.isEmpty == false) {
      val fileReadData = FileReadData(string=readDataToCallback,
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

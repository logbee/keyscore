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



object FileReader {
  
  private def getRotatedFiles(baseFile: File, rotationPattern: String): Array[File] = {
    rotationPattern match {
      case "" =>
        Array()
      case null =>
        Array()
        
      case rotationPattern =>
        val filesInSameDir = baseFile.toPath.getParent.resolve(rotationPattern).getParent.toFile.listFiles //resolve a relative path, if the rotationPattern contains one
        
        if (filesInSameDir == null) //if the directory doesn't exist
          Array()
        else {
          val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + baseFile.getParent + "/" + rotationPattern)
          
          filesInSameDir.filter(fileInSameDir => rotateMatcher.matches(fileInSameDir.toPath))
        }
    }
  }
  
  
  
  /**
   * Returns the given {@code baseFile} as well as any rotated files, which have been modified more recently than or exactly at the {@code previousReadTimestamp}.
   * 
   * It also returns the file which has been lastModified at the {@code previousReadTimestamp} (which we don't need to read from anymore),
   * as we would otherwise continue reading at the {@code previousReadPosition} in the new file.
   * 
   * The files are sorted by their lastModified-timestamp, from oldest to newest.
   */
  def getFilesToRead(baseFile: File, rotationPattern: String, previousReadTimestamp: Long): Array[File] = {
    val rotatedFiles = getRotatedFiles(baseFile, rotationPattern)
    
    val files = if (rotatedFiles contains baseFile)
                   rotatedFiles
                else
                   (rotatedFiles :+ baseFile)
    
    val filesToRead = files.filter(file => file.lastModified >= previousReadTimestamp) // '>=' to include the last-read file, in case it hasn't been written to anymore. This simplifies dealing with the case where such a last-read identical file has been rotated away, as we then want to start the newly created file from the beginning, not the previousReadPosition
    
    filesToRead.sortBy(file => file.lastModified) //sort from oldest to newest
  }
}


/**
 * @param rotationPattern Glob-pattern for the file-name of rotated files. If an empty string or null is passed, no rotated files are matched.
 */
class FileReader(baseFile: File, rotationPattern: String, byteBufferSize: Int, charset: Charset, readMode: ReadMode) {
  
  private val log = LoggerFactory.getLogger(classOf[FileReader])
  
  log.info("Instantiated for " + baseFile)
  var leftOverFromPreviousBuffer = ""
  
  
  //TODO this can be removed, maybe reused in FileReaderManager
//  var fileReadRecord: FileReadRecord = FileReadRecord(0, 0) //the file hasn't yet been persisted, or something went wrong, which we can't recover from
//  if (persistenceContext != null) {
//    val loadedFileReadRecord = persistenceContext.load[FileReadRecord](watchedFile.getAbsolutePath)
//    loadedFileReadRecord match {
//      case None =>
//      case Some(loadedFileReadRecord: FileReadRecord) =>
//        fileReadRecord = loadedFileReadRecord
//    }
//  }
  
  

  
  
  def read(callback: FileReadData => Unit, readScheduleItem: ReadScheduleItem) = {
    
    log.info("fileModified() called for " + baseFile)
    
    val decoder = charset.newDecoder
    decoder.onMalformedInput(CodingErrorAction.REPORT)
    
    val charBufferSize = Math.ceil(byteBufferSize * charset.newDecoder.maxCharsPerByte).asInstanceOf[Int] //enough space to decode a full byteBuffer
    val charBuffer = CharBuffer.allocate(charBufferSize)
    
    val byteBuffer = ByteBuffer.allocate(byteBufferSize)
    
    
    
    val filesToRead = FileReader.getFilesToRead(baseFile, rotationPattern, readScheduleItem.writeTimestamp)
    val file = filesToRead.head
    
    println("fileReader-endPos: " + readScheduleItem.endPos + ", file-length: " + file.length)
    assert(readScheduleItem.endPos <= file.length) //TODO
    
    var nextBufferStartPosition = readScheduleItem.startPos
    
    if (nextBufferStartPosition < readScheduleItem.endPos) { //skip creating a fileReadChannel and persisting the data, if there is nothing to read
      var fileReadChannel: FileChannel = null
      try {
        fileReadChannel = Files.newByteChannel(file.toPath, StandardOpenOption.READ).asInstanceOf[FileChannel]
        
        
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
          
          processBufferContents(charBuffer, callback, nextBufferStartPosition, readScheduleItem.writeTimestamp)
          
          
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
  
  
  
  private def processBufferContents(buffer: CharBuffer, callback: FileReadData => Unit, startPositionInFile: Long, callbackWriteTimestamp: Long) = {
      
    if (readMode == ReadMode.LINE) {
      
      var writtenPositionWithinBuffer = 0
      
      while (buffer.position < buffer.limit) {
        
        //check for the occurrence of \n or \r, as we do linewise reading
        val byte = buffer.get().toChar //sets pos to pos+1
        
        if (byte == '\n' || byte == '\r') {
  
          val lengthToWrite = buffer.position - 1 - writtenPositionWithinBuffer // "-1", because we don't want to count the '\n'
          
          val string = CharBufferUtil.getBufferSectionAsString(buffer, writtenPositionWithinBuffer, lengthToWrite)
          
          writtenPositionWithinBuffer += lengthToWrite
          writtenPositionWithinBuffer = CharBufferUtil.getStartOfNextLine(buffer, writtenPositionWithinBuffer)
          
          doCallback(callback, string, startPositionInFile + writtenPositionWithinBuffer, callbackWriteTimestamp)
          
          buffer.position(writtenPositionWithinBuffer)
        }
      }
      
      //if end of buffer reached without finding another newline
      buffer.position(writtenPositionWithinBuffer) //reset to the previous written position, so that the rest of the buffer can be written
    }
    else if (readMode == ReadMode.FILE) {
      //do nothing
      buffer.position(0)
    }
    
    //end of data in buffer reached
    
    
    val length = buffer.limit - buffer.position
    
    if (length > 0) {
      val string = CharBufferUtil.getBufferSectionAsString(buffer, buffer.position, length)
      
      
      if (buffer.limit < buffer.capacity) { //end of file
        
        doCallback(callback, string, startPositionInFile + buffer.limit, callbackWriteTimestamp) //write the remaining bytes
      }
      else { //not end of file
        
        leftOverFromPreviousBuffer += string //store the remaining bytes, to be written later
      }
    }
  }
  
  
  private def doCallback(callback: FileReadData => Unit, string: String, writeEndPos: Long, writeTimestamp: Long) = {
    
    val fileReadData = FileReadData(leftOverFromPreviousBuffer + string, baseFile, writeEndPos, writeTimestamp)
    
    callback(fileReadData)
    leftOverFromPreviousBuffer = ""
  }
  
  
  def pathDeleted() {??? //TODO
//    if (FileReader.getFilesToRead(baseFile, rotationPattern, 0).length == 0) { //if no rotated files remain
//      persistenceContext.remove(baseFile.toString)
//    }
    tearDown()
  }
  
  def tearDown() = {
    log.info("Teardown for " + baseFile)
  }
}

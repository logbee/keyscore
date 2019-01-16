package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File
import java.nio.channels.FileChannel
import java.nio.charset.{CharacterCodingException, Charset, CodingErrorAction}
import java.nio.file.{FileSystems, Files, StandardOpenOption}
import java.nio.{ByteBuffer, CharBuffer}

import io.logbee.keyscore.pipeline.contrib.tailin.util.CharBufferUtil
import io.logbee.keyscore.pipeline.contrib.tailin.file.ReadMode.ReadMode
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import org.slf4j.LoggerFactory


object ReadMode extends Enumeration {
  type ReadMode = Value
  val LINE, FILE = Value
}

case class FileReadRecord(previousReadPosition: Long, previousReadTimestamp: Long)



object FileReader {
  
  private def getRotatedFiles(file: File, rotationPattern: String): Array[File] = {
    rotationPattern match {
      case "" =>
        Array()
      case null =>
        Array()
        
      case rotationPattern =>
        val filesInSameDir = file.toPath.getParent.resolve(rotationPattern).getParent.toFile.listFiles //resolve a relative path, if the rotationPattern contains one
        
        if (filesInSameDir == null) //if the directory doesn't exist
          Array()
        else {
          val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + file.getParent + "/" + rotationPattern)
          
          filesInSameDir.filter(fileInSameDir => rotateMatcher.matches(fileInSameDir.toPath))
        }
    }
  }
  
  
  
  /**
   * Returns the given mainFile as well as any rotated files, which have been more recently modified than the previousReadTimestamp.
   * 
   * The files are sorted by their lastModified-timestamp, from oldest to newest.
   */
  def getFilesToRead(mainFile: File, rotationPattern: String, previousReadTimestamp: Long): Array[File] = {
    val rotatedFiles = getRotatedFiles(mainFile, rotationPattern)
    
    val files = if (rotatedFiles contains mainFile)
                   rotatedFiles
                else
                   (rotatedFiles :+ mainFile)
    
    val filesToRead = files.filter(_file => _file.lastModified >= previousReadTimestamp) // '>=' to include the last-read file, in case it hasn't been written to anymore. This simplifies dealing with the case where such a last-read identical file has been rotated away, as we then want to start the newly created file from the beginning, not the previousReadPosition
    
    filesToRead.sortBy(_file => _file.lastModified) //sort from oldest to newest
  }
}


/**
 * @param rotationPattern Glob-pattern for the suffix of rotated files. If an empty string or null is passed, no rotated files are matched.
 * @param persistenceContext PersistenceContext where RotationRecords are stored and read from.
 */
class FileReader(watchedFile: File, rotationPattern: String, persistenceContext: PersistenceContext, byteBufferSize: Int, charset: Charset, readMode: ReadMode) extends DefaultFileWatcher(watchedFile) with FileWatcher {
  
  private val log = LoggerFactory.getLogger(classOf[FileReader])
  
  log.info("Instantiated for " + watchedFile)
  var leftOverFromPreviousBuffer = ""
  
  
  
  
  var rotationRecord: FileReadRecord = FileReadRecord(0, 0) //the file hasn't yet been persisted, or something went wrong, which we can't recover from
  if (persistenceContext != null) {
    val loadedRotationRecord = persistenceContext.load[FileReadRecord](watchedFile.toString)
    loadedRotationRecord match {
      case None =>
      case Some(loadedRotationRecord: FileReadRecord) =>
        rotationRecord = loadedRotationRecord
    }
  }
  
  

  
  
  def fileModified(callback: String => Unit) = {
    
    log.info("fileModified() called for " + watchedFile)
    
    val decoder = charset.newDecoder()
    decoder.onMalformedInput(CodingErrorAction.REPORT)
    
    val charBufferSize = Math.ceil(byteBufferSize * charset.newDecoder().maxCharsPerByte).asInstanceOf[Int] //enough space to decode a full byteBuffer
    val charBuffer = CharBuffer.allocate(charBufferSize)
    
    val byteBuffer = ByteBuffer.allocate(byteBufferSize)
    
    
    
    val filesToRead = FileReader.getFilesToRead(watchedFile, rotationPattern, rotationRecord.previousReadTimestamp)
    filesToRead.foreach { file =>
      
      var nextBufferStartPosition = 0L
      if (file.equals(filesToRead.head)) { //if this is the first file to be read, read from the previousReadPosition
        nextBufferStartPosition = rotationRecord.previousReadPosition
      }
      
      if (nextBufferStartPosition < file.length) { //skip creating a fileReadChannel and persisting the data, if there is nothing to read
        var fileReadChannel: FileChannel = null
        try {
          fileReadChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.READ).asInstanceOf[FileChannel]
          
          
          while (nextBufferStartPosition < file.length) {
            
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
            
            processBufferContents(charBuffer, callback)
            
            
            nextBufferStartPosition += bytesRead
          }
        }
        finally {
          if (fileReadChannel != null) {
            fileReadChannel.close()
          }
          
          
          if (file.equals(filesToRead.last)) {//if this is the last file to be read, persist the new readPosition and readTimestamp
            
            rotationRecord = rotationRecord.copy(previousReadPosition = nextBufferStartPosition,
                                                 previousReadTimestamp = watchedFile.lastModified)
            
            persistenceContext.store(watchedFile.getAbsolutePath, rotationRecord)
          }
        }
      }
    }//continue in loop with next file
  }
  
  
  
  private def processBufferContents(buffer: CharBuffer, callback: String => Unit) = {
      
    if (readMode == ReadMode.LINE) {
      
      var writtenPositionWithinBuffer = 0
      
      while (buffer.position() < buffer.limit()) {
        
        //check for the occurrence of \n or \r, as we do linewise reading
        val byte = buffer.get().toChar //sets pos to pos+1
        
        if (byte == '\n' || byte == '\r') {
  
          val lengthToWrite = buffer.position() - 1 - writtenPositionWithinBuffer // "-1", because we don't want to count the '\n'
          
          val string = CharBufferUtil.getBufferSectionAsString(buffer, writtenPositionWithinBuffer, lengthToWrite)
          
          doCallback(callback, string)
          
          writtenPositionWithinBuffer += lengthToWrite
          writtenPositionWithinBuffer = CharBufferUtil.getStartOfNextLine(buffer, writtenPositionWithinBuffer)
          
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
    
    
    val length = buffer.limit() - buffer.position()
    
    if (length > 0) {
      val string = CharBufferUtil.getBufferSectionAsString(buffer, buffer.position, length)
      
      
      if (buffer.limit() < buffer.capacity) { //end of file
        
        doCallback(callback, string) //write the remaining bytes
      }
      else { //not end of file
        
        leftOverFromPreviousBuffer += string //store the remaining bytes, to be written later
      }
    }
  }
  
  
  private def doCallback(callback: String => Unit, string: String) = {
    
    callback(leftOverFromPreviousBuffer + string)
    leftOverFromPreviousBuffer = ""
  }
  
  
  def pathDeleted() {
    if (FileReader.getFilesToRead(watchedFile, rotationPattern, rotationRecord.previousReadTimestamp).length == 0) { //if no rotated files remain
      persistenceContext.remove(watchedFile.toString)
    }
    tearDown()
  }
  
  def tearDown() = {
    log.info("Teardown for " + watchedFile)
  }
}

package io.logbee.keyscore.pipeline.contrib.tailin.read

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper
import io.logbee.keyscore.pipeline.contrib.tailin.watch.ReadScheduler.FileInfo
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


class FileReaderManager(fileReaderProvider: FileReaderProvider, readSchedule: ReadSchedule, readPersistence: ReadPersistence, rotationPattern: String) {
  private lazy val log = LoggerFactory.getLogger(classOf[FileReaderManager])

  private val fileReaders = Map[FileHandle, FileReader]()
  
  private def getFileReader(fileToRead: FileHandle): FileReader = {
    val fileReaderOpt = fileReaders.get(fileToRead)
    
    var fileReader: FileReader = null
    if (fileReaderOpt.isDefined) {
      fileReader = fileReaderOpt.get
    }
    else { //create a new fileReader, if there's not yet one in the map
      fileReader = fileReaderProvider.create(fileToRead)
      fileReaders + (fileToRead -> fileReader)
    }
    
    fileReader
  }
  
  
  def getNextString(callback: FileReadData => Unit): Unit = {
    
    readSchedule.dequeue() match {
      case None => //if no reads scheduled
        //do nothing, rescheduling is triggered by caller
        
      case Some(readScheduleItem) =>
        
        val baseFile = readScheduleItem.baseFile
        
        val completedRead = readPersistence.getCompletedRead(baseFile)
        
        val fileToRead = baseFile.open {
          case Success(openBaseFile) =>
            val baseFileWithInfo = FileInfo(baseFile, openBaseFile.name, openBaseFile.lastModified, openBaseFile.length)
            RotationHelper.getRotationFilesToRead(openBaseFile, baseFileWithInfo, rotationPattern, completedRead).head

          case Failure(ex) =>
            log.error("Could not retrieve information about file that was scheduled to read.", ex)
            return
        }

        val callback2: FileReadData => Unit =
          fileReadData => {
            callback(fileReadData.copy(baseFile=baseFile)) //fileReader doesn't know what the baseFile is
          }
        
        getFileReader(fileToRead.file).read(callback2, readScheduleItem)
    }
  }
}

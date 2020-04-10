package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.{ReadPersistence, ReadSchedule, ReadScheduleItem}
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper.ListRotatedFilesException
import io.logbee.keyscore.pipeline.contrib.tailin.watch.FileReadScheduler.FileInfo
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object FileReadScheduler {
  case class FileInfo(file: FileHandle, name: String, lastModified: Long, length: Long)
}

class FileReadScheduler(baseFile: FileHandle, rotationPattern: String, readPersistence: ReadPersistence, readSchedule: ReadSchedule) extends FileEventHandler {
  private lazy val log = LoggerFactory.getLogger(classOf[FileReadScheduler])
  
  private var previouslyScheduled = readPersistence.getCompletedRead(baseFile)
  
  
  def processChanges(): Try[Unit] = {
    
    val (baseFileWithInfo, filesToRead) = baseFile.open {
      case Success(openBaseFile) =>
        val baseFileWithInfo = FileInfo(baseFile, openBaseFile.name, openBaseFile.lastModified, openBaseFile.length)
        (
          baseFileWithInfo,
          try {
            RotationHelper.getRotationFilesToRead(openBaseFile, baseFileWithInfo, rotationPattern, previouslyScheduled)
          }
          catch {
            case ex: ListRotatedFilesException => return Failure(ex)
          }
        )
      case Failure(ex) =>
        log.error(s"Failed to open base file: $baseFile", ex)
        return Failure(ex)
    }

    //baseFile can still be written to, meaning its lastModified-timestamp could change at any point in the future
    //therefore, if files share their lastModified-timestamp with the baseFile,
    //the 'newest' file (lowest or no rotation-index in file-name) with this shared lastModified-timestamp could still change.
    //We rely on this to not change anymore to be able to differentiate them (via another index - the number of newerFilesWithSharedLastModified).
    val filesToSchedule = {
      val filtered = filesToRead.filter(_.lastModified != baseFileWithInfo.lastModified)

      if (filesToRead.length - filtered.length > 1)
        filtered
      else
        filesToRead
    }
    
    if (filesToSchedule.isEmpty)
      return Success(())
    
    
    //check for files which have the same lastModified-time (which we need to differentiate in order to tell them apart)
    val filesToScheduleGroupedByLastModified = filesToSchedule
                                                 .groupBy(_.lastModified) //convert to map lastModified -> Array[File]
                                                 .toSeq.sortBy(_._1) //convert to list of tuples (lastModified, Array[File]) and sort it by lastModified-time
    
    

    //do the scheduling
    var startPos = previouslyScheduled.previousReadPosition
    
    filesToScheduleGroupedByLastModified.foreach {
      case (lastModified, fileToScheduleGroup) =>
        var newerFilesWithSharedLastModified = fileToScheduleGroup.length
        
        fileToScheduleGroup.foreach { fileToSchedule =>
          newerFilesWithSharedLastModified -= 1
          
          val endPos = fileToSchedule.length
          if (startPos != endPos || lastModified != previouslyScheduled.previousReadTimestamp) { //else assume this is the same file that we previously read from -> theoretically it is possible for this to be wrong, when in the same second (or whatever the filesystem's time resolution is) a file with the same length is created
            if (startPos > endPos) startPos = 0 //assume that the file got truncated and start reading from the beginning again
            
            readSchedule.enqueue(ReadScheduleItem(baseFile, startPos, endPos, lastModified, newerFilesWithSharedLastModified))
            
            previouslyScheduled = FileReadRecord(endPos, lastModified, newerFilesWithSharedLastModified)
          }
          
          startPos = 0 //if there's multiple files, read the next files from the start
        }
    }
    
    Success(())
  }
}




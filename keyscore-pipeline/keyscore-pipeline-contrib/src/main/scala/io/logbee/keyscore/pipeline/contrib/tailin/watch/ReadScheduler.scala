package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper


class ReadScheduler(baseFile: FileHandle, rotationPattern: String, readPersistence: ReadPersistence, readSchedule: ReadSchedule) extends FileEventHandler {
  
  var previouslyScheduled = readPersistence.getCompletedRead(baseFile)
  
  
  def processFileChanges(): Unit = {
    
    //getFilesToRead also returns files which have lastModified == previousReadTimestamp, as multiple files may have the same lastModified-time
    //and this helps to simplify the code, because then we know to not continue reading at the previousReadPosition in the next file
    val filesToRead = RotationHelper.getRotationFilesToRead(baseFile, rotationPattern, previouslyScheduled)
    
    
    var filesToSchedule = filesToRead
    //baseFile can still be written to, meaning its lastModified-timestamp could change at any point in the future
    //therefore, if files share their lastModified-timestamp with the baseFile,
    //the 'newest' (lowest rotation-index in file-name) file with this shared lastModified-timestamp could still change.
    //We rely on this to not change anymore to be able to differentiate them (via another index - the number of newerFilesWithSharedLastModified).
    if (filesToRead.filter(_.lastModified == baseFile.lastModified).size > 1) {
      filesToSchedule = filesToSchedule.filter(_.lastModified != baseFile.lastModified)
    }
    
    if (filesToSchedule.isEmpty)
      return
    
    
    //check for files which have the same lastModified-time (which we need to differentiate in order to tell them apart)
    val filesToScheduleGroupedByLastModified = filesToSchedule
                                                 .groupBy(file => file.lastModified) //convert to map lastModified -> Array[File]
                                                 .toSeq.sortBy(_._1) //convert to list of tuples (lastModified, Array[File]) and sort it by lastModified-time
    
    
    
    
    
    
    //do the scheduling
    var startPos = previouslyScheduled.previousReadPosition
    
    filesToScheduleGroupedByLastModified.foreach {
      case (lastModified, fileToScheduleGroup) =>
        var newerFilesWithSharedLastModified = fileToScheduleGroup.length
        
        fileToScheduleGroup
          .foreach { fileToSchedule =>
            newerFilesWithSharedLastModified -= 1
            
            val endPos = fileToSchedule.length
            if (startPos != endPos) {
//            if (startPos < endPos) {
              val lastModified = fileToSchedule.lastModified
              readSchedule.enqueue(ReadScheduleItem(baseFile, startPos, endPos, lastModified, newerFilesWithSharedLastModified))
              
              previouslyScheduled = FileReadRecord(endPos, lastModified, newerFilesWithSharedLastModified)
            }
        //      else { // >= the file got truncated //TODO the TailinSourceLogicSpec for some reason causes files with file-length 0 to be pushed into here?
        //        //assume that log rotation happened
        //        println("fileToRead: " + fileToRead + ", startPos: " + startPos + ", endPos: " + endPos);
        //        ??? //TODO maybe deal with this somehow
        //      }
            
            startPos = 0 //if there's multiple files, read the next files from the start
        }
    }
  }
  
  
  def pathDeleted(): Unit = {
    tearDown()
  } //TODO delete entries from file or somehow tell fileReader to teardown (FileReader should probably finish reading out rotated files, if those haven't been deleted yet, before it does so)
                               //in general, FileReader should have a mechanism of dealing with missing files, too.
  
  def tearDown(): Unit = {
    baseFile.tearDown()
  }
}

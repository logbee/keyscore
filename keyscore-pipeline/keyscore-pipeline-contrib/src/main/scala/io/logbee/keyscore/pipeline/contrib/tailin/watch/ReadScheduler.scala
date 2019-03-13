package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper


class ReadScheduler(baseFile: File, rotationPattern: String, readPersistence: ReadPersistence, readSchedule: ReadSchedule) extends FileWatcher {
  
  var previouslyScheduled = readPersistence.getCompletedRead(baseFile)
  
  
  def fileModified(): Unit = {
    
    //getFilesToRead also returns files which have lastModified == previousReadTimestamp, as multiple files may have the same lastModified-time
    //and this helps to simplify the code, because then we know to not continue reading at the previousReadPosition in the next file
    val filesToRead = RotationHelper.getFilesToRead(baseFile, rotationPattern, previouslyScheduled.previousReadTimestamp) //TODO pass along previousScheduled.newerFilesWithSharedLastModified and use that on the other side to filter out files, too
    
    
    
    var filesToSchedule = filesToRead
    if (filesToRead.filter(_.lastModified == baseFile.lastModified).size > 1) { //TODO document this
      filesToSchedule = filesToSchedule.filter(_.lastModified != baseFile.lastModified)
    }
    
    if (filesToSchedule.isEmpty)
      return
    
    
    //check for files which have the same lastModified-time (which we need to differentiate in order to tell them apart)
    val filesToScheduleGroupedByLastModified = filesToSchedule
                                                 .groupBy(file => file.lastModified) //convert to map lastModified -> Array[File]
                                                 .toSeq.sortBy(_._1) //convert to list of tuples (lastModified, Array[File]) and sort it by lastModified-time
    

    
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
  
  
  def pathDeleted(): Unit = {} //TODO delete entries from file or somehow tell fileReader to teardown (FileReader should probably finish reading out rotated files, if those haven't been deleted yet, before it does so)
                               //in general, FileReader should have a mechanism of dealing with missing files, too.
  
  def tearDown(): Unit = {}
}

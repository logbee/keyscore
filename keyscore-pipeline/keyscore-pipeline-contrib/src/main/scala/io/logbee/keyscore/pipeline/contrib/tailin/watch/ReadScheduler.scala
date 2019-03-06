package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import scala.collection.SortedMap


class ReadScheduler(baseFile: File, rotationPattern: String, readPersistence: ReadPersistence, readSchedule: ReadSchedule) extends FileWatcher {
  
  var previouslyScheduled = readPersistence.getCompletedRead(baseFile)
  
  
  def fileModified(): Unit = {
    
    
    val filesToRead = RotationHelper.getFilesToRead(baseFile, rotationPattern, previouslyScheduled.previousReadTimestamp)
    //getFilesToRead also returns files which have lastModified == previousReadTimestamp (which we would technically not need to read, but helps simplify this situation)
    
    
    //check for files which have the same lastModified-time (which we need to differentiate in order to tell them apart)
    val filesToReadGroupedByLastModified = filesToRead
                                             .groupBy(file => file.lastModified) //convert to map lastModified -> Array[File]
                                             .toSeq.sortBy(_._1) //convert to list of tuples (lastModified, Array[File]) and sort it by lastModified-time
                                             
  //TODO might have to sort the entries in the shared-lastModified groups, too (by filename)  
                                             
    val filesToReadWithSharedLastModified = filesToReadGroupedByLastModified
                                              .filter(group => group._2.length > 1) //filter out any files where there are not multiple ones with the same lastModified
    
    if (filesToReadWithSharedLastModified.nonEmpty) {
      
      val sharedLastModifiedTimes = filesToReadWithSharedLastModified.unzip._1
      
      if (sharedLastModifiedTimes.contains(System.currentTimeMillis / 1000)) {
        //TODO wait for one second,
        //to ensure that no later created files can get this same lastModified-time
        println("2KJlaskdjalksdjasdkljas\n\n\n\n\naskljsdhakjsdhaksjd\n\n\n\nkajshdkashdaksdjh")
      }
      
      
      if (sharedLastModifiedTimes.contains(baseFile.lastModified)) {
        println("3aslkjdlaksjdalsjdaoisjqoj\n\n\nljkasdlkjasda\n\n\naskalsd")
        
        
        //we have to wait until the baseFile (.0) is modified or rotated to .1, so that the set of files with shared lastModified is fixed (apart from deletion, which we can deal with)
        return
        //-> a future fileEvent will rotate .0 away or append something to it, giving it a different lastModified-time
        //and therefore ensuring that no other files can get this same shared lastModified-time afterwards
        //this and the fact that we can assume that .2 is older than .1, is older than .0 etc.,
        //allows us to reference these files by this shared lastModified-time and how far one has to count from the lowest shared-lastModified-index
        //TODO adjust comment for better understandability
      }
    }
    
    
    
    var startPos = previouslyScheduled.previousReadPosition
    
    
    filesToReadGroupedByLastModified.foreach {
      case (lastModified, fileToReadGroup) =>
        var newerFilesWithSharedLastModified = fileToReadGroup.length
        
        fileToReadGroup
          .foreach { fileToRead =>
            newerFilesWithSharedLastModified -= 1
            
            val endPos = fileToRead.length
            if (startPos != endPos) {
//            if (startPos < endPos) {
              val lastModified = fileToRead.lastModified
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

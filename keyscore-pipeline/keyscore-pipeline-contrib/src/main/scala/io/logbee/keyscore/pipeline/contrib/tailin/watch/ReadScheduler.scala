package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import java.nio.file.FileSystems
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotationHelper


class ReadScheduler(baseFile: File, rotationPattern: String, readPersistence: ReadPersistence, readSchedule: ReadSchedule) extends FileWatcher {
  
  //TODO detect truncate somehow
  //probably store baseFile's length with each fileModified()
  //on resume, restore from getCompletedRead?> under given circumstances probably,
    //like no other rotatedFiles having been lastModified after that timestamp
  
  //when new fileModified's length is shorter than previous lastModify, assume truncate
  
  
  def fileModified(): Unit = {
    
    val completedRead = readPersistence.getCompletedRead(baseFile)
    

    val filesToRead = RotationHelper.getFilesToRead(baseFile, rotationPattern, completedRead.previousReadTimestamp).sortBy(file => file.getName).reverse //assume the files are created in order .1, .2, .3, etc.
    
    
    var startPos = completedRead.previousReadPosition
    
    filesToRead.foreach{ fileToRead =>
      
      if (startPos != fileToRead.length) { //getFilesToRead returns files which have lastModified == previousReadTimestamp (which we would technically not need to read, but helps simplify this situation)
        val endPos = fileToRead.length
        val timestamp = fileToRead.lastModified
        
        readSchedule.push(ReadScheduleItem(fileToRead, startPos, endPos, timestamp))
      }
      startPos = 0 //if there's multiple files, read the next files from the start
    }
  }
  
  
  def pathDeleted(): Unit = {} //TODO delete entries from file or somehow tell fileReader to teardown (FileReader should probably finish reading out rotated files, if those haven't been deleted yet, before it does so)
                               //in general, FileReader should have a mechanism of dealing with missing files, too.
  
  def tearDown(): Unit = {}
}
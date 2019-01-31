package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.RAMPersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence


class FileReaderManager(readSchedule: ReadSchedule, fileReaderProvider: FileReaderProvider) {
  
  
  //what happens if the files get rotated? (i.e. moved underneath the file-handle)
  //does the fileChannel remain untouched?
  
  
  val map = Map[File, FileReader]()
  
  
  
  def getNextString(callback: FileReadData => Unit) = {
    
    //dequeue the next schedule entry
    val readScheduleItemOpt = readSchedule.pop()
    
    readScheduleItemOpt match {
      case Some(readScheduleItem) =>
        
        val baseFile = readScheduleItem.file
        
        var fileReaderOpt = map.get(baseFile)
        
        var fileReader: FileReader = null
        if (fileReaderOpt == Some) {
          fileReader = fileReaderOpt.get
        }
        else { //create a new fileReader, if there's not yet one in the map
          fileReader = fileReaderProvider.create(baseFile)
          map + (baseFile -> fileReader)
        }
        
        
        fileReader.read(callback, readScheduleItem)
        
      case None =>//if no reads scheduled
       //TODO schedule a dirWatcher.processEvents and a retry
       // -> scheduling a retry can only be done in TailinSourceLogic (?)
        // -> we might not have to do anything here
       
       
       //TODO maybe pass this method a callback that will doPush()
       // that way it's non-blocking, and can at any moment when ready do the push
       // and therefore could be timered every 1 second or something
       //  Can we then guarantee, though, that it doesn't push twice?
       //  I think, we can, because only one timeline is going to be active
    }
    
    
    //TODO do the black magic where you find out the correct FileReader
    // maybe only do half the black magic here
    // and half the black magic in a RotationReaderHandler-class
    //  let's do the 50% solution first, where we pass it directly to the FileReader and assume no rotation
    //  the RotationReaderHandler-class should be easy to introduce in between
  }
}

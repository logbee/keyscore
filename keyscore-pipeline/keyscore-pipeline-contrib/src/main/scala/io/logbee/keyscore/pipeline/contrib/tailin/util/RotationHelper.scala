package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.io.File
import java.nio.file.FileSystems
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord

object RotationHelper {
  
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
          
          val rotatedFilesInSameDir = filesInSameDir.filter(fileInSameDir => rotateMatcher.matches(fileInSameDir.toPath))
          
          rotatedFilesInSameDir
        }
    }
  }
  
  
  /**
   * Returns the given {@code baseFile} as well as any related rotated files, which have been modified more recently than or exactly at the {@code previousReadTimestamp}.
   * 
   * It also returns the file which has been lastModified at the {@code previousReadRecord.previousReadTimestamp},
   * as we would otherwise continue reading at the {@code previousReadPosition} in the new file and because sometimes files will have the same lastModified-timestamp.
   * 
   * The files are sorted by their lastModified-timestamp, from oldest to newest.
   * If the lastModified-timestamp is equivalent for two files, they are sorted by their file-name, so that e.g. a file with rotation-index .2 is returned before .1, as .2 should have been written to earlier
   */
  def getRotationFilesToRead(baseFile: File, rotationPattern: String, previousReadRecord: FileReadRecord): Array[File] = {
    
    val rotatedFiles = getRotatedFiles(baseFile, rotationPattern)
    
    
    
    val files = if (rotatedFiles contains baseFile)
                   rotatedFiles
                else
                   (rotatedFiles :+ baseFile)
    
    
    val filesWithCorrectLastModified = files.filter(file => file.lastModified >= previousReadRecord.previousReadTimestamp) // '>=' to include the last-read file, in case it hasn't been written to anymore. This simplifies dealing with the case where such a last-read identical file has been rotated away, as we then want to start the newly created file from the beginning, not the previousReadPosition
    
    val sortedFilesWithCorrectLastModified = filesWithCorrectLastModified.sortBy(file => (file.lastModified, file.getName))(Ordering.Tuple2(Ordering.Long, Ordering.String.reverse)) //if lastModified-timestamps are equivalent for two files, assume that e.g. .2 was written to before .1
    //TODO sorting alphabetically is probably not enough, as .2 will come before .10
    
    
    var filesToRead = sortedFilesWithCorrectLastModified
    
    //deal with files that share their lastModified-timestamp
    val filesWithSharedLastModified = sortedFilesWithCorrectLastModified.filter(_.lastModified == previousReadRecord.previousReadTimestamp)
    
    filesToRead = filesToRead.drop(filesWithSharedLastModified.size - previousReadRecord.newerFilesWithSharedLastModified - 1)
    
    
    filesToRead
  }
}
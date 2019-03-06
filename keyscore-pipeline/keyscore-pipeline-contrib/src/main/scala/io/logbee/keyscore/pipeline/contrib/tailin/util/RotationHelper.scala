package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.io.File
import java.nio.file.FileSystems

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
          
          filesInSameDir.filter(fileInSameDir => rotateMatcher.matches(fileInSameDir.toPath))
        }
    }
  }
  
  //TODO adjust description -> with the need to differentiate between rotationFiles that have the same lastModified-time, this actually becomes important
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
    
    files.filter(file => file.lastModified >= previousReadTimestamp) // '>=' to include the last-read file, in case it hasn't been written to anymore. This simplifies dealing with the case where such a last-read identical file has been rotated away, as we then want to start the newly created file from the beginning, not the previousReadPosition
  }
}

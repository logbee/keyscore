package io.logbee.keyscore.pipeline.contrib.tailin.util

import io.logbee.keyscore.pipeline.contrib.tailin.file.OpenFileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.watch.ReadScheduler.FileInfo
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


object RotationHelper {
  
  private lazy val log = LoggerFactory.getLogger(RotationHelper.getClass)

  case class ListRotatedFilesException(message: String, throwable: Throwable) extends RuntimeException


  /**
   * Returns the given {@code baseFile} as well as any related rotated files, which have been modified more recently than or exactly at the {@code previousReadTimestamp}.
   * 
   * It also returns the file which has been lastModified at the {@code previousReadRecord.previousReadTimestamp},
   * as we would otherwise continue reading at the {@code previousReadPosition} in the new file and because sometimes files will have the same lastModified-timestamp.
   * 
   * The files are sorted by their lastModified-timestamp, from oldest to newest.
   * If the lastModified-timestamp is equivalent for two files, they are sorted by their file-name, so that e.g. a file with rotation-index .2 is returned before .1, as .2 should have been written to earlier
   */
  def getRotationFilesToRead(openBaseFile: OpenFileHandle, baseFileWithInfo: FileInfo, rotationPattern: String, previousReadRecord: FileReadRecord): Seq[FileInfo] = {
    
    val rotatedFilesWithInfo =
      openBaseFile
        .listRotatedFiles(rotationPattern)
        .map(file => file.open {
          case Success(openFile) => FileInfo(file, openFile.name, openFile.lastModified, openFile.length)
          case Failure(ex) =>
            throw ListRotatedFilesException(s"Could not open rotated file: $file", ex)
        })
    
    
    
    val files = if (rotatedFilesWithInfo contains baseFileWithInfo)
                  rotatedFilesWithInfo
                else
                  rotatedFilesWithInfo :+ baseFileWithInfo
    
    
    val filesWithCorrectLastModified = files.filter(_.lastModified >= previousReadRecord.previousReadTimestamp) // '>=' to include the last-read file, in case it hasn't been written to anymore. This simplifies dealing with the case where such a last-read identical file has been rotated away, as we then want to start the newly created file from the beginning, not the previousReadPosition
    
    val sortedFilesWithCorrectLastModified = filesWithCorrectLastModified.sortBy(file => (file.lastModified, file.name))(Ordering.Tuple2(Ordering.Long, Ordering.String.reverse)) //if lastModified-timestamps are equivalent for two files, assume that e.g. .2 was written to before .1
    //TODO sorting alphabetically is probably not enough, as .2 will come before .10
    

    //deal with files that share their lastModified-timestamp
    val filesWithSharedLastModified = sortedFilesWithCorrectLastModified.filter(_.lastModified == previousReadRecord.previousReadTimestamp)
    
    sortedFilesWithCorrectLastModified.drop(filesWithSharedLastModified.size - previousReadRecord.newerFilesWithSharedLastModified - 1)
  }
}

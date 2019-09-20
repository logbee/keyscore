package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import io.logbee.keyscore.pipeline.contrib.tailin.file._
import io.logbee.keyscore.pipeline.contrib.tailin.watch.WatchDirNotFoundException
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

class SmbDirChangeListener(dir: SmbDir) extends DirChangeListener[SmbDir, SmbFile](dir) {
  
  private lazy val log = LoggerFactory.getLogger(classOf[SmbDirChangeListener])
  
  private var (previousSubDirs, previousSubFiles): (Seq[SmbDir], Seq[SmbFile]) = //TODO get this from persistence
    dir.open {
      case Success(dir) => dir.listDirsAndFiles
      case Failure(ex) =>
        log.warn(s"Could not retrieve existing subDirs and subFiles for '$dir'. Treating every subDir and subFile as new.", ex)
        (Seq.empty, Seq.empty)
    }
  
  
  
  override def getChanges: DirChanges[SmbDir, SmbFile] = {

    var changes: DirChanges[SmbDir, SmbFile] = null
    
    try {
      val (currentSubDirs, currentSubFiles): (Seq[SmbDir], Seq[SmbFile]) = dir.open {
        case Success(dir) => dir.listDirsAndFiles
        case Failure(ex) =>
          log.warn(s"Could not retrieve subDirs and subFiles for '$dir'. Assuming no changes from previous listing.", ex)
          (previousSubDirs, previousSubFiles)
      }
      
      //determine dir-changes
      var deletedPaths: Seq[PathHandle] = previousSubDirs.diff(currentSubDirs)
      val dirsContinuingToExist = previousSubDirs.intersect(currentSubDirs)
      val newlyCreatedDirs = currentSubDirs.diff(previousSubDirs)

      previousSubDirs = currentSubDirs

      //determine file-changes
      deletedPaths = deletedPaths ++ previousSubFiles.diff(currentSubFiles)
      val filesContinuingToExist = previousSubFiles.intersect(currentSubFiles)
      val newlyCreatedFiles = currentSubFiles.diff(previousSubFiles)
  
      previousSubFiles = currentSubFiles
      
      changes = DirChanges(
        newlyCreatedDirs,
        newlyCreatedFiles,
        deletedPaths,
        dirsContinuingToExist,
        filesContinuingToExist,
      )
    }
    catch {
      case ex: SMBApiException =>
        ex.getStatus match {
          case NtStatus.STATUS_OBJECT_NAME_NOT_FOUND =>
            log.error(ex.getMessage)
            throw WatchDirNotFoundException()

          case _ =>
            throw ex
        }
    }
    
    changes
  }
  
  
  override def tearDown(): Unit = {}
}

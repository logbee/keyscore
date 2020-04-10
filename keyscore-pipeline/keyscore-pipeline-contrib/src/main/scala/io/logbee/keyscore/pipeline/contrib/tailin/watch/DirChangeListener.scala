package io.logbee.keyscore.pipeline.contrib.tailin.watch

import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import io.logbee.keyscore.pipeline.contrib.tailin.file._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}


case class DirChanges[D <: DirHandle[D, F], F <: FileHandle](
   newlyCreatedDirs: Seq[D],
   newlyCreatedFiles: Seq[F],
   deletedPaths: Seq[_ <: PathHandle],
   potentiallyModifiedDirs: Seq[D],
   potentiallyModifiedFiles: Seq[F],
)


class DirChangeListener[
    D <: DirHandle[D, F],
    F <: FileHandle
  ]
  (dir: D) {
  
  private lazy val log = LoggerFactory.getLogger(classOf[DirChangeListener[D, F]])
  
  private var (previousSubDirs, previousSubFiles): (Seq[D], Seq[F]) = //TODO get this from persistence
    dir.open {
      case Success(dir) => dir.listDirsAndFiles
      case Failure(ex) =>
        log.warn(s"Could not retrieve existing subDirs and subFiles for '$dir'. Treating every subDir and subFile as new.", ex)
        (Seq.empty, Seq.empty)
    }
  
  
  
  def computeChanges: Try[DirChanges[D, F]] = {
    try {
      val (currentSubDirs, currentSubFiles): (Seq[D], Seq[F]) = dir.open {
        case Success(dir) => dir.listDirsAndFiles
        case Failure(ex) => return Failure(ex)
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
      
      
      Success(
        DirChanges(
          newlyCreatedDirs,
          newlyCreatedFiles,
          deletedPaths,
          dirsContinuingToExist,
          filesContinuingToExist,
        )
      )
    }
    catch {
      case ex: SMBApiException =>
        ex.getStatus match {
          case NtStatus.STATUS_OBJECT_NAME_NOT_FOUND =>
            log.error(ex.getMessage)
            Failure(WatchDirNotFoundException())

          case _ =>
            Failure(ex)
        }
    }
  }
}

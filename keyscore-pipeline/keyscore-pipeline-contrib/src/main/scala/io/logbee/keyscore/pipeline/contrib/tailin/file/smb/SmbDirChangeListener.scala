package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirChangeListener, DirChanges, PathHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.watch.WatchDirNotFoundException
import org.slf4j.LoggerFactory

class SmbDirChangeListener(dir: SmbDir) extends DirChangeListener(dir) {
  
  private lazy val log = LoggerFactory.getLogger(classOf[SmbDirChangeListener])
  
  private var (previousSubDirs, previousSubFiles) = (Set[SmbDir](), Set[SmbFile]())
  
  
  
  override def getChanges: DirChanges = {
  
    var changes: DirChanges = null
    
    try {
      val (currentSubDirs, currentSubFiles) = dir.listDirsAndFiles
      
      //determine dir-changes
      var deletedPaths: Set[PathHandle] = previousSubDirs.toSeq.diff(currentSubDirs.toSeq).toSet
      val dirsContinuingToExist = previousSubDirs.intersect(currentSubDirs)
      val newlyCreatedDirs = currentSubDirs.diff(previousSubDirs)
  
      previousSubDirs = currentSubDirs
  
      //determine file-changes
      deletedPaths = deletedPaths ++ previousSubFiles.diff(currentSubFiles)
      val filesContinuingToExist = previousSubFiles.intersect(currentSubFiles)
      val newlyCreatedFiles = currentSubFiles.diff(previousSubFiles)
  
      previousSubFiles = currentSubFiles
      
      changes = DirChanges(newlyCreatedDirs,
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
            throw new WatchDirNotFoundException(dir)

          case _ =>
            throw ex
        }
    }
    
    changes
  }
  
  
  override def tearDown(): Unit = {}
}

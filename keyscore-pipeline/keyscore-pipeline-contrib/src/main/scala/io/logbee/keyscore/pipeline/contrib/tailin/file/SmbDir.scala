package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.util.EnumSet

import scala.collection.JavaConverters

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.common.SmbPath
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.File
import io.logbee.keyscore.pipeline.contrib.tailin.watch.DirChanges


class SmbDir(dir: Directory) extends DirHandle {
  
  var (previousSubDirs, previousSubFiles) = listDirsAndFiles
  
  
  override def absolutePath = dir.getFileName
  
  
  override def listDirsAndFiles: (Seq[SmbDir], Seq[SmbFile]) = { //TODO try to make private or remove
    
    val subPaths = JavaConverters.asScalaBuffer(dir.list).toSeq
                     .filterNot(subPath => subPath.getFileName.endsWith("\\.")
                                        || subPath.getFileName.equals(".")
                                        || subPath.getFileName.endsWith("\\..")
                                        || subPath.getFileName.equals("..")
                                )
    
    
    var dirs: Seq[SmbDir] = Seq.empty
    var files: Seq[SmbFile] = Seq.empty
    
    subPaths.foreach { subPath =>
      val dirPathName = SmbPath.parse(absolutePath).getPath //just the directory's name, i.e. not the absolute path
      
      try {
        val diskEntry = share.open(
          dirPathName + subPath.getFileName,
          EnumSet.of(AccessMask.GENERIC_ALL),
          EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
          SMB2ShareAccess.ALL,
          SMB2CreateDisposition.FILE_OPEN,
          EnumSet.noneOf(classOf[SMB2CreateOptions])
        )
        
        
        if (diskEntry.isInstanceOf[Directory]) {
          dirs = dirs :+ new SmbDir(diskEntry.asInstanceOf[Directory])
        } else {
          files = files :+ new SmbFile(diskEntry.asInstanceOf[File])
        }
      }
      catch {
        case ex: SMBApiException =>
          if (ex.getStatus == NtStatus.STATUS_DELETE_PENDING) {
            //this file is being deleted, so don't add it to the listing
          }
          else {
            throw ex
          }
      }
    }
    
    (dirs, files)
  }
  
  
  
  
  def getChanges: DirChanges = {
    
    val (currentSubDirs, currentSubFiles) = listDirsAndFiles
    
    
    //process dir-changes
    var deletedPaths: Seq[PathHandle] = previousSubDirs.diff(currentSubDirs)
    val dirsContinuingToExist = previousSubDirs.intersect(currentSubDirs)
    val newlyCreatedDirs = currentSubDirs.diff(previousSubDirs)
    
    previousSubDirs = currentSubDirs
    
    
    //process file-changes
    deletedPaths = deletedPaths ++ previousSubFiles.diff(currentSubFiles)
    val filesContinuingToExist = previousSubFiles.intersect(currentSubFiles)
    val newlyCreatedFiles = currentSubFiles.diff(previousSubFiles)
    
    previousSubFiles = currentSubFiles
    
    
    DirChanges(newlyCreatedDirs,
               newlyCreatedFiles,
               deletedPaths,
               dirsContinuingToExist,
               filesContinuingToExist,
              )
  }
  
  
  def tearDown() = {
    if (dir != null) {
      dir.close()
    }
  }
  
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[SmbDir]
  
  override def equals(other: Any): Boolean = other match {
    case that: SmbDir =>
      (that canEqual this) &&
        this.absolutePath == that.absolutePath// &&
//        this.share.getSmbPath.equals(that.share.getSmbPath)
    case _ => false
  }
  
  override def hashCode(): Int = {
    val state = Seq(this.absolutePath/*, this.share*/)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
  
  
  private def share = dir.getDiskShare
}

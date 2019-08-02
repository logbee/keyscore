package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.util.EnumSet

import scala.collection.JavaConverters
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.common.SmbPath
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskShare
import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.PathHandle
import io.logbee.keyscore.pipeline.contrib.tailin.watch.DirChanges

import scala.jdk.javaapi.CollectionConverters


class SmbDir(path: String, share: DiskShare) extends DirHandle {
  
  
  private lazy val dir: Directory = share.openDirectory(
    path,
    EnumSet.of(AccessMask.GENERIC_READ),
    EnumSet.of(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
    SMB2ShareAccess.ALL,
    SMB2CreateDisposition.FILE_OPEN,
    EnumSet.noneOf(classOf[SMB2CreateOptions])
  )
  
  
  private var (previousSubDirs, previousSubFiles) = listDirsAndFiles
  
  
  override def absolutePath = dir.getFileName
  
  
  /**
   * \\hostname\share\path\to\ -> path\to\
   */
  private def pathWithinShare: String = SmbPath.parse(absolutePath).getPath
  
  
  private def isDirectory(fileIdBothDirectoryInformation: FileIdBothDirectoryInformation): Boolean = {
    import com.hierynomus.msfscc.FileAttributes._
    (fileIdBothDirectoryInformation.getFileAttributes & FILE_ATTRIBUTE_DIRECTORY.getValue) == FILE_ATTRIBUTE_DIRECTORY.getValue
  }
  
  
  override def listDirsAndFiles: (Set[SmbDir], Set[SmbFile]) = {
    
    val subPaths = CollectionConverters.asScala(dir.list).toSet
                     .filterNot(subPath => subPath.getFileName.endsWith("\\.")
                                        || subPath.getFileName.equals(".")
                                        || subPath.getFileName.endsWith("\\..")
                                        || subPath.getFileName.equals("..")
                                )
    
    
    var dirs: Set[SmbDir] = Set.empty
    var files: Set[SmbFile] = Set.empty
    
    subPaths.foreach { subPath =>
      
      try {
        if (isDirectory(subPath)) {
          dirs = dirs + new SmbDir(pathWithinShare + subPath.getFileName + "\\", share)
        } else {
          files = files + new SmbFile(pathWithinShare + subPath.getFileName, share)
        }
      }
      catch {
        case smbException: SMBApiException =>
          if (smbException.getStatus == NtStatus.STATUS_DELETE_PENDING) {
            //this file/dir is being deleted, so don't add it to the listing
          }
          else {
            throw smbException
          }
      }
    }
    
    (dirs, files)
  }
  
  
  
  
  def getChanges: DirChanges = {
    
    val (currentSubDirs, currentSubFiles) = listDirsAndFiles
    
    
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
}

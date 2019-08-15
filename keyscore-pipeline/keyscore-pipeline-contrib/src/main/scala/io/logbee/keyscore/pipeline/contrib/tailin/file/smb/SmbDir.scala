package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.util.EnumSet

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
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirChangeListener, DirHandle}
import org.slf4j.LoggerFactory

import scala.jdk.javaapi.CollectionConverters


class SmbDir(path: String, share: DiskShare) extends DirHandle {

  private lazy val log = LoggerFactory.getLogger(classOf[SmbDir])


  private def withDir[T](func: Directory => T): T = {
    var dir: Directory = null

    try {
      dir = share.openDirectory(
        path,
        EnumSet.of(AccessMask.GENERIC_ALL),
        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
        SMB2ShareAccess.ALL,
        SMB2CreateDisposition.FILE_OPEN,
        EnumSet.noneOf(classOf[SMB2CreateOptions])
      )

      func(dir)
    }
    finally {
      if (dir != null)
        dir.close()
    }
  }
  
  
  override val absolutePath: String = withDir(_.getFileName)
  
  /**
   * \\hostname\share\path\to\ -> path\to\
   */
  private val pathWithinShare: String = SmbPath.parse(absolutePath).getPath
  
  
  
  
  private def isDirectory(fileIdBothDirectoryInformation: FileIdBothDirectoryInformation): Boolean = {
    import com.hierynomus.msfscc.FileAttributes._
    (fileIdBothDirectoryInformation.getFileAttributes & FILE_ATTRIBUTE_DIRECTORY.getValue) == FILE_ATTRIBUTE_DIRECTORY.getValue
  }
  
  
  override def listDirsAndFiles: (Set[SmbDir], Set[SmbFile]) = {
    withDir(dir => {

      val subPaths = CollectionConverters.asScala(dir.list).toSet
        .filterNot(subPath =>
             subPath.getFileName.endsWith("\\.")
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
            smbException.getStatus match {
              case NtStatus.STATUS_DELETE_PENDING =>
                //this file/dir is being deleted, so don't add it to the listing
                log.debug("Listed dir which is pending to be deleted.")
                
              case NtStatus.STATUS_OBJECT_NAME_NOT_FOUND =>
                //this file/dir is already deleted, so don't add it to the listing
                log.debug("Listed dir which does not exist.")
                
              case _ =>
                log.error("Uncaught SMBApiException while listing files and dirs: " + smbException.getMessage)
                throw smbException
            }
            
          case otherException: Throwable =>
            log.error("Uncaught exception while listing files and dirs: " + otherException.getMessage)
            throw otherException
        }
      }
      
      (dirs, files)
    })
  }


  override def getDirChangeListener(): DirChangeListener = new SmbDirChangeListener(this)
  
  
  override def tearDown(): Unit = {}
  
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[SmbDir]
  
  override def equals(other: Any): Boolean = other match {
    case that: SmbDir =>
      (that canEqual this) &&
        this.absolutePath.equals(that.absolutePath)// &&
//        this.share.getSmbPath.equals(that.share.getSmbPath)
    case _ => false
  }
  
  override def hashCode(): Int = {
    val state = Seq(this.absolutePath/*, this.share*/)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
  
  override def toString: String = {
    absolutePath
  }
}
